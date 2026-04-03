package com.example.fitnestx

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var spinnerRoutines: Spinner
    private lateinit var spinnerExercises: Spinner
    private lateinit var chart: LineChart
    private lateinit var database: DatabaseReference
    private var allSessions = mutableListOf<WorkoutSession>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        spinnerRoutines = findViewById(R.id.spinnerRoutines)
        spinnerExercises = findViewById(R.id.spinnerExercises)
        chart = findViewById(R.id.performanceChart)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().getReference("WorkoutSessions").child(userId)

        loadSessionsAndSetupRoutines()
    }

    private fun loadSessionsAndSetupRoutines() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allSessions.clear()
                val routineNames = mutableSetOf<String>()

                for (ds in snapshot.children) {
                    val session = ds.getValue(WorkoutSession::class.java)
                    session?.let {
                        allSessions.add(it)
                        routineNames.add(it.routineName)
                    }
                }

                if (routineNames.isEmpty()) return

                val routineAdapter = ArrayAdapter(this@AnalyticsActivity, android.R.layout.simple_spinner_item, routineNames.toList())
                routineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerRoutines.adapter = routineAdapter

                spinnerRoutines.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        setupExerciseSpinner(routineNames.toList()[p2])
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupExerciseSpinner(routineName: String) {
        val exerciseNames = mutableSetOf<String>()

        allSessions.filter { it.routineName == routineName }.forEach { session ->
            session.exercises.forEach { exerciseNames.add(it.exerciseName) }
        }

        val exerciseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseNames.toList())
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExercises.adapter = exerciseAdapter

        spinnerExercises.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                updateChartForExercise(routineName, exerciseNames.toList()[p2])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun updateChartForExercise(routineName: String, exerciseName: String) {
        val filteredSessions = allSessions.filter { it.routineName == routineName }
            .sortedBy { it.timestamp }

        val actualEntries = ArrayList<Entry>()
        val plannedEntries = ArrayList<Entry>()
        val xAxisLabels = ArrayList<String>()

        var globalSetCounter = 0f

        filteredSessions.forEach { session ->
            val exercise = session.exercises.find { it.exerciseName == exerciseName }

            exercise?.let { ex ->
                ex.sets.forEachIndexed { setIndex, setData ->
                    // સેટ મુજબ ડેટા એન્ટ્રી
                    actualEntries.add(Entry(globalSetCounter, setData.actualReps.toFloat()))
                    plannedEntries.add(Entry(globalSetCounter, setData.plannedReps.toFloat()))

                    // X-axis લેબલ: "S1-Set1" (Session 1, Set 1)
                    xAxisLabels.add("Set ${setIndex + 1}")

                    globalSetCounter++
                }
            }
        }

        if (actualEntries.isEmpty()) {
            chart.clear()
            return
        }

        val actualSet = LineDataSet(actualEntries, "$exerciseName (Actual)").apply {
            color = Color.parseColor("#2ECC71") // Green
            setCircleColor(Color.parseColor("#2ECC71"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            mode = LineDataSet.Mode.LINEAR // સેટ-વાઈઝમાં Linear વધુ સ્પષ્ટ દેખાય છે
        }

        val plannedSet = LineDataSet(plannedEntries, "Target").apply {
            color = Color.GRAY
            setCircleColor(Color.GRAY)
            lineWidth = 2f
            enableDashedLine(10f, 5f, 0f)
            setDrawValues(false)
        }

        // X-Axis Config - આ ભાગને અપડેટ કરો
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)

            // આ નવો સુરક્ષિત Formatter છે
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    // અહીં ચેક કરીએ છીએ કે ઇન્ડેક્સ લિસ્ટની રેન્જમાં છે કે નહીં
                    return if (index >= 0 && index < xAxisLabels.size) {
                        xAxisLabels[index]
                    } else {
                        "" // જો ઇન્ડેક્સ ખોટો હોય તો ખાલી જગ્યા બતાવો, ક્રેશ ના કરો
                    }
                }
            }
        }

        chart.axisRight.isEnabled = false
        chart.description.text = "Set-by-set Performance"
        chart.data = LineData(plannedSet, actualSet)
        chart.animateX(800)
        chart.invalidate()
    }
}
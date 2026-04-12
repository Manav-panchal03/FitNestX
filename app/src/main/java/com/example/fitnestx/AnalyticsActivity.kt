package com.example.fitnestx

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val routinesRef = FirebaseDatabase.getInstance().getReference("Routines").child(userId)

        // ૧. પેલા ચેક કરો કે અત્યારે કયા રૂટિન એક્ઝિસ્ટ કરે છે
        routinesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(routineSnapshot: DataSnapshot) {
                val activeRoutineNames = mutableSetOf<String>()
                for (ds in routineSnapshot.children) {
                    val name = ds.child("routineName").getValue(String::class.java)
                    name?.let { activeRoutineNames.add(it) }
                }

                // ૨. હવે સેશન્સ લોડ કરો અને માત્ર એક્ટિવ રૂટિનના જ સેશન્સ ફિલ્ટર કરો
                database.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(sessionSnapshot: DataSnapshot) {
                        allSessions.clear()
                        val filteredRoutineNames = mutableSetOf<String>()

                        for (ds in sessionSnapshot.children) {
                            val session = ds.getValue(WorkoutSession::class.java)
                            // ફિલ્ટર લોજિક: જો રૂટિન હજુ લિસ્ટમાં હોય તો જ સેશન લેવું
                            if (session != null && activeRoutineNames.contains(session.routineName)) {
                                allSessions.add(session)
                                filteredRoutineNames.add(session.routineName)
                            }
                        }

                        if (filteredRoutineNames.isEmpty()) {
                            spinnerRoutines.adapter = null
                            spinnerExercises.adapter = null
                            chart.clear()
                            return
                        }

                        updateRoutineSpinner(filteredRoutineNames.toList())
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateRoutineSpinner(routineList: List<String>) {
        val routineAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, routineList)
        routineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRoutines.adapter = routineAdapter

        spinnerRoutines.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                setupExerciseSpinner(routineList[p2])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupExerciseSpinner(routineName: String) {
        val exerciseNames = mutableSetOf<String>()
        allSessions.filter { it.routineName == routineName }.forEach { session ->
            session.exercises.forEach { exerciseNames.add(it.exerciseName) }
        }

        val exerciseList = exerciseNames.toList()
        val exerciseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseList)
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExercises.adapter = exerciseAdapter

        spinnerExercises.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                updateChartForExercise(routineName, exerciseList[p2])
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
                    actualEntries.add(Entry(globalSetCounter, setData.actualReps.toFloat()))
                    plannedEntries.add(Entry(globalSetCounter, setData.plannedReps.toFloat()))
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
            color = Color.parseColor("#2ECC71")
            setCircleColor(Color.parseColor("#2ECC71"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawValues(true)
            valueTextSize = 10f
            mode = LineDataSet.Mode.LINEAR
        }

        val plannedSet = LineDataSet(plannedEntries, "Target").apply {
            color = Color.GRAY
            setCircleColor(Color.GRAY)
            lineWidth = 2f
            enableDashedLine(10f, 5f, 0f)
            setDrawValues(false)
        }

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < xAxisLabels.size) xAxisLabels[index] else ""
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
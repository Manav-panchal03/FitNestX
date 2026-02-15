package com.example.fitnestx

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class WeightProgressGraphActivity : AppCompatActivity() {

    private lateinit var chart: LineChart
    private lateinit var dbWeight: DatabaseReference
    private lateinit var dbUser: DatabaseReference

    private var startingWeight = 0.0
    private var goalWeight = 0.0
    private var currentWeight = 0.0

    private val timestamps = ArrayList<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_progress_graph)

        chart = findViewById(R.id.weightChart)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        dbWeight = FirebaseDatabase.getInstance()
            .getReference("WeightHistory")
            .child(uid)

        dbUser = FirebaseDatabase.getInstance()
            .getReference("AppUsers")
            .child(uid)

        loadUserData()
    }

    // =========================
    // USER DATA
    // =========================
    private fun loadUserData() {

        dbUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                startingWeight =
                    snapshot.child("startingWeight")
                        .getValue(Double::class.java) ?: 0.0

                goalWeight =
                    snapshot.child("goalWeight")
                        .getValue(Double::class.java) ?: 0.0

                currentWeight =
                    snapshot.child("weight")
                        .getValue(Double::class.java) ?: 0.0

                loadGraphData()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // =========================
    // GRAPH DATA
    // =========================
    private fun loadGraphData() {

        dbWeight.orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val entries = ArrayList<Entry>()
                    timestamps.clear()

                    var index = 0f

                    for (snap in snapshot.children) {

                        val weight =
                            snap.child("weight")
                                .getValue(Double::class.java)

                        val time =
                            snap.child("timestamp")
                                .getValue(Long::class.java)

                        if (weight != null && time != null) {
                            entries.add(
                                Entry(index, weight.toFloat())
                            )
                            timestamps.add(time)
                            index++
                        }
                    }

                    setupChart(entries)
                    showProgressText()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =========================
    // SETUP CHART ⭐
    // =========================
    private fun setupChart(entries: List<Entry>) {

        if (entries.isEmpty()) return

        val dataSet = LineDataSet(entries, "Weight")

        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.lineWidth = 4f
        dataSet.circleRadius = 6f
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(true)

        val lineData = LineData(dataSet)
        chart.data = lineData

        // Clean UI
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false

        // X axis ⭐
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setLabelCount(timestamps.size, true)
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = (timestamps.size - 1).toFloat()
        xAxis.labelRotationAngle = -0f

        xAxis.valueFormatter =
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {

                    val index = value.toInt()

                    if (index < 0 ||
                        index >= timestamps.size
                    ) return ""

                    return formatDate(
                        timestamps[index]
                    )
                }
            }

        // Y padding
        val yAxis = chart.axisLeft
        yAxis.axisMinimum =
            entries.minOf { it.y } - 1
        yAxis.axisMaximum =
            entries.maxOf { it.y } + 1

        // Disable zoom
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        chart.animateY(1000)
        chart.invalidate()
    }

    // =========================
    // PROGRESS
    // =========================
    private fun calculateProgress(): Int {

        if (goalWeight == 0.0 ||
            startingWeight == goalWeight
        ) return 0

        val progress =
            if (goalWeight > startingWeight) {
                (currentWeight - startingWeight) /
                        (goalWeight - startingWeight)
            } else {
                (startingWeight - currentWeight) /
                        (startingWeight - goalWeight)
            }

        return (progress * 100)
            .coerceIn(0.0, 100.0)
            .toInt()
    }

    private fun showProgressText() {

        val progress = calculateProgress()

        val tvProgress =
            findViewById<TextView>(R.id.tvProgress)

        val tvMotivation =
            findViewById<TextView>(R.id.tvMotivation)

        tvProgress.text =
            "$progress% progress"

        tvMotivation.text = when {
            progress == 0 ->
                "🚀 Stay consistent!"

            progress < 50 ->
                "💪 Good progress!"

            progress < 100 ->
                "🔥 Almost there!"

            else ->
                "👑 Goal achieved!"
        }
    }

    // =========================
    // DATE FORMAT
    // =========================
    private fun formatDate(
        time: Long
    ): String {

        val sdf =
            SimpleDateFormat(
                "dd MMM",
                Locale.getDefault()
            )

        return sdf.format(Date(time))
    }
}

package com.example.fitnestx

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitStatsActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var tvTotal: TextView
    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_habit_stats)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        barChart = findViewById(R.id.habitBarChart)
        tvTotal = findViewById(R.id.tvTotalCompleted)

        setupChartAppearance()
        fetchMonthlyData()

    }


    private fun setupChartAppearance() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setTouchEnabled(true)

        // --- Monthly mate Scrollable banavva mate ---
        barChart.setScaleEnabled(true)
        barChart.setPinchZoom(false)
        barChart.isDragEnabled = true // User slide kari shakse

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = android.graphics.Color.GRAY
        xAxis.labelRotationAngle = -25f // Dates ne thodi trasi rakhiye jethi jagya male

        barChart.axisRight.isEnabled = false
        barChart.axisLeft.textColor = android.graphics.Color.GRAY
        barChart.axisLeft.axisMinimum = 0f
    }

    private fun fetchMonthlyData() {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMM", Locale.getDefault()) // 01 Jan, 02 Jan...

        val last30Days = mutableListOf<String>()
        for (i in -29..0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, i)
            last30Days.add(sdf.format(calendar.time))
            labels.add(displayFormat.format(calendar.time))
        }

        database.child("DailyHabitLogs").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalCount = 0

                    last30Days.forEachIndexed { index, dateKey ->
                        val daySnapshot = snapshot.child(dateKey)
                        val completedCount = daySnapshot.children.count {
                            it.getValue(Boolean::class.java) == true
                        }

                        totalCount += completedCount
                        entries.add(BarEntry(index.toFloat(), completedCount.toFloat()))
                    }

                    updateChart(entries, labels)
                    tvTotal.text = "Monthly Progress: $totalCount habits completed! 🚀"
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateChart(entries: ArrayList<BarEntry>, labels: ArrayList<String>) {
        val dataSet = BarDataSet(entries, "Monthly Habit Progress")
        dataSet.color = android.graphics.Color.parseColor("#92A3FD")
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // --- IMPORTANT: Zoom set karo jethi bar nani na thai jay ---
        // 30 divas mathi ek sathe 7 divas dekhase, baaki na slide karva padse
        barChart.setVisibleXRangeMaximum(7f)
        barChart.moveViewToX(entries.size.toFloat()) // Graph ne latest date par lai jase

        barChart.animateY(1000)
        barChart.invalidate()
    }
}
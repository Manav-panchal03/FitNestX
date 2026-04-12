package com.example.fitnestx

import android.graphics.Color
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

        val dataSet = LineDataSet(entries, "Weight Progress")

// ૧. રેખાની જાડાઈ વધારો (૫-૬f રાખવી વધુ સ્પષ્ટ દેખાશે)
        dataSet.lineWidth = 3f

// ૨. રેખાનો કલર ઘટ્ટ કરો (દા.ત. તમારા થીમ મુજબનો ઘટ્ટ કે વાદળી)
        dataSet.color = Color.parseColor("#00CCFF") // ઘટ્ટ વાદળી

// ૩. ડેટા પોઈન્ટ્સ (Circles) પર વધુ ફોકસ કરો
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 6f // વર્તુળો થોડા મોટા કરો
        dataSet.setCircleColor(Color.parseColor("#00CCFF")) // વર્તુળોનો કલર રેખા જેવો જ
        dataSet.circleHoleColor = Color.WHITE // વર્તુળની વચ્ચે વાદળી હોલ રાખો
        dataSet.circleHoleRadius = 4f
        dataSet.setDrawCircleHole(true) // હોલ સાથે વર્તુળ દેખાશે

// ૪. ડેટા રેખાની ઉપર વેલ્યુ લખવી હોય તો તે ચાલુ કરો
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 12f  // વેલ્યુ થોડી મોટી કરો
        dataSet.valueTextColor = Color.BLACK // કાળી વેલ્યુ વધુ સારી લાગશે
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${String.format("%.1f", value)} kg" // વેલ્યુની પાછળ kg લખો
            }
        }
        // ... બાકીનું ડેટાસેટ લોજિક એમનેમ રાખો ...

        val lineData = LineData(dataSet)
        chart.data = lineData

        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false

        // ⭐ ગ્રાફની નીચે વધારે જગ્યા આપો કારણ કે ઉભી તારીખો વધારે જગ્યા રોકશે
        chart.setExtraOffsets(0f, 0f, 0f, 40f)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // ⭐ દરેક પોઈન્ટ પર લેબલ બતાવવા માટે Granularity 1 રાખો
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        // ⭐ આ લાઈન કાઢી નાખજો અથવા 'timestamps.size' લખજો જેથી બધી જ તારીખ દેખાય
        xAxis.setLabelCount(timestamps.size, false)

        // ⭐ તારીખોને પૂરેપૂરી ઉભી (-90) કરી નાખો
        xAxis.labelRotationAngle = -90f

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                if (index >= 0 && index < timestamps.size) {
                    // અહીં તમે "dd MMM" વાપરી શકો છો (દા.ત. 07 Apr)
                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                    return sdf.format(Date(timestamps[index]))
                }
                return ""
            }
        }

        // Y axis અને એનિમેશન
        chart.axisLeft.setDrawGridLines(true)
        chart.animateY(1000)

        // ⭐ જો ડેટા બહુ વધારે હોય તો યુઝરને સ્ક્રોલ કરવાની સુવિધા આપો
        if (entries.size > 7) {
            chart.setVisibleXRangeMaximum(7f) // એકસાથે ૭ તારીખો જ દેખાશે, બાકીની સ્ક્રોલ કરીને જોઈ શકાશે
            chart.moveViewToX(entries.size.toFloat()) // લેટેસ્ટ ડેટા પર ફોકસ રાખશે
        }

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
    private fun formatDateShort(time: Long): String {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        return sdf.format(Date(time))
    }
}

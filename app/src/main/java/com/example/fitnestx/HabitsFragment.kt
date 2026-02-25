package com.example.fitnestx

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class HabitsFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var dateText: TextView

    // UI Elements
    private lateinit var layoutCompletedHeader: LinearLayout
    private lateinit var tvCompletedCount: TextView
    private lateinit var ivArrow: ImageView

    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val habits = mutableListOf<Habit>()
    private val completionMap = mutableMapOf<String, Boolean>()

    private var habitsListener: ValueEventListener? = null
    private var dailyListener: ValueEventListener? = null

    private var showChecked = true

    // --- New: Selected Date state ---
    private var selectedDateKey: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        // Bind Views
        recycler = view.findViewById(R.id.habitRecycler)
        layoutCompletedHeader = view.findViewById(R.id.layoutCompletedHeader)
        ivArrow = view.findViewById(R.id.ivArrow)
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount)
        dateText = view.findViewById(R.id.dateText)

        val tvMarquee = view.findViewById<TextView>(R.id.tvHabitMarquee)
        tvMarquee.isSelected = true

        tvMarquee.setOnClickListener {
            startActivity(Intent(requireContext(), HabitStatsActivity::class.java))
        }

        // Initialize with Today's Date
        val today = LocalDate.now()
        selectedDateKey = today.toString() // yyyy-MM-dd
        dateText.text = today.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"))

        // --- Date Picker Click ---
        dateText.setOnClickListener {
            showCalendarDialog()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = HabitAdapter(habits, completionMap) { habit, checked ->
            saveCompletion(habit.id, checked)
        }

        recycler.adapter = adapter

        val animator = DefaultItemAnimator()
        animator.moveDuration = 350
        animator.changeDuration = 250
        recycler.itemAnimator = animator

        loadHabits()
        loadDataByDate(selectedDateKey)

        view.findViewById<MaterialButton>(R.id.manageHabitBtn).setOnClickListener {
            startActivity(Intent(requireContext(), ManageHabitActivity::class.java))
        }

        layoutCompletedHeader.setOnClickListener {
            showChecked = !showChecked
            adapter.toggleCheckedVisibility(showChecked)
            ivArrow.animate().rotation(if (showChecked) 0f else -90f).setDuration(250).start()
        }

        return view
    }

    private fun showCalendarDialog() {
        val calendar = Calendar.getInstance()
        val picker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val cal = Calendar.getInstance()
            cal.set(year, month, day)

            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())

            selectedDateKey = apiFormat.format(cal.time)
            dateText.text = displayFormat.format(cal.time)

            // Re-load data for new date
            loadDataByDate(selectedDateKey)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        picker.show()
    }

    private fun loadHabits() {
        habitsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentIds = mutableSetOf<String>()
                habits.clear()
                for (child in snapshot.children) {
                    val habitId = child.key!!
                    val name = child.child("name").getValue(String::class.java)
                    if (name != null) {
                        habits.add(Habit(habitId, name))
                        currentIds.add(habitId)
                    }
                }
                cleanupOrphanedLogs(currentIds)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("Habits").orderByChild("userId").equalTo(userId).addValueEventListener(habitsListener!!)
    }

    private fun loadDataByDate(date: String) {
        // Remove old listener if exists
        dailyListener?.let { database.child("DailyHabitLogs").child(userId).child(selectedDateKey).removeEventListener(it) }

        dailyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completionMap.clear()
                val activeIds = habits.map { it.id }.toSet()
                for (child in snapshot.children) {
                    val id = child.key!!
                    val value = child.getValue(Boolean::class.java)
                    if (value != null && activeIds.contains(id)) {
                        completionMap[id] = value
                    }
                }
                adapter.notifyDataSetChanged()
                updateCheckedHeader()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("DailyHabitLogs").child(userId).child(date).addValueEventListener(dailyListener!!)
    }

    private fun cleanupOrphanedLogs(currentHabitIds: Set<String>) {
        val ref = database.child("DailyHabitLogs").child(userId).child(selectedDateKey)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (log in snapshot.children) {
                    if (!currentHabitIds.contains(log.key)) {
                        ref.child(log.key!!).removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveCompletion(habitId: String, checked: Boolean) {
        database.child("DailyHabitLogs").child(userId).child(selectedDateKey).child(habitId).setValue(checked)
    }

    private fun updateCheckedHeader() {
        val count = completionMap.values.count { it }
        layoutCompletedHeader.visibility = if (count > 0) View.VISIBLE else View.GONE
        tvCompletedCount.text = "$count completed"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        habitsListener?.let { database.child("Habits").removeEventListener(it) }
        dailyListener?.let { database.child("DailyHabitLogs").child(userId).child(selectedDateKey).removeEventListener(it) }
    }
}
package com.example.fitnestx

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
        // ... inside onCreateView ...
        val tvMarquee = view.findViewById<TextView>(R.id.tvHabitMarquee)
        tvMarquee.isSelected = true // Aa line Marquee chalu karva mate jaruri che

        tvMarquee.setOnClickListener {
            val intent = Intent(requireContext(), HabitStatsActivity::class.java)
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Adapter setup with completion callback
        adapter = HabitAdapter(habits, completionMap) { habit, checked ->
            saveCompletion(habit.id, checked)
            // Header count update thase jyare koi check/uncheck thase
        }

        recycler.adapter = adapter

        // Smooth animations jem Google Keep ma hoy che
        val animator = DefaultItemAnimator()
        animator.moveDuration = 350
        animator.changeDuration = 250
        recycler.itemAnimator = animator

        // Date Display logic
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")
        dateText.text = today.format(formatter)

        loadHabits()
        loadTodayData()

        view.findViewById<MaterialButton>(R.id.manageHabitBtn).setOnClickListener {
            startActivity(Intent(requireContext(), ManageHabitActivity::class.java))
        }

        // --- Google Keep Toggle Logic ---
        layoutCompletedHeader.setOnClickListener {
            showChecked = !showChecked
            adapter.toggleCheckedVisibility(showChecked)

            // Khali arrow rotate thase, text hamesha seedhu rahese
            ivArrow.animate()
                .rotation(if (showChecked) 0f else -90f)
                .setDuration(250)
                .start()
        }

        return view
    }

    private fun todayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // 1. loadHabits ma sudharo: Deleted habits na logs clean karva mate
    private fun loadHabits() {
        habitsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentHabitIds = mutableSetOf<String>()
                habits.clear()

                for (child in snapshot.children) {
                    val habitId = child.key!!
                    val name = child.child("name").getValue(String::class.java)
                    if (name != null) {
                        habits.add(Habit(habitId, name))
                        currentHabitIds.add(habitId)
                    }
                }

                // --- Extra Cleaning Logic ---
                // Jo koi habit log ma che pan "Habits" list mathi delete thai gai che, to ene kadhi nakho
                cleanupOrphanedLogs(currentHabitIds)

                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        database.child("Habits")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(habitsListener!!)
    }

    // 2. Aa navu function add karo je orphaned logs delete karse
    private fun cleanupOrphanedLogs(currentHabitIds: Set<String>) {
        val todayRef = database.child("DailyHabitLogs").child(userId).child(todayDate())

        todayRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (log in snapshot.children) {
                    val habitIdInLog = log.key ?: continue

                    // Jo aa habitId main list ma nathi, to log mathi remove karo
                    if (!currentHabitIds.contains(habitIdInLog)) {
                        todayRef.child(habitIdInLog).removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 3. loadTodayData ma pan filter muko
    private fun loadTodayData() {
        dailyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completionMap.clear()

                // Fakt e j habits load karo je hal ma exists kare che
                val habitIds = habits.map { it.id }.toSet()

                for (child in snapshot.children) {
                    val habitId = child.key!!
                    val value = child.getValue(Boolean::class.java)

                    // Filter: Jo habit delete thai gai hoy to map ma na umeryo
                    if (value != null && habitIds.contains(habitId)) {
                        completionMap[habitId] = value
                    }
                }
                adapter.notifyDataSetChanged()
                updateCheckedHeader()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        database.child("DailyHabitLogs")
            .child(userId)
            .child(todayDate())
            .addValueEventListener(dailyListener!!)
    }

    private fun saveCompletion(habitId: String, checked: Boolean) {
        database.child("DailyHabitLogs")
            .child(userId)
            .child(todayDate())
            .child(habitId)
            .setValue(checked)
    }

    private fun updateCheckedHeader() {
        val count = completionMap.values.count { it }
        if (count > 0) {
            layoutCompletedHeader.visibility = View.VISIBLE
            tvCompletedCount.text = "$count completed"
        } else {
            layoutCompletedHeader.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        habitsListener?.let { database.child("Habits").removeEventListener(it) }
        dailyListener?.let {
            database.child("DailyHabitLogs").child(userId).child(todayDate()).removeEventListener(it)
        }
    }
}
package com.example.fitnestx

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class HabitsFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: HabitAdapter

    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val habits = mutableListOf<Habit>()
    private val completionMap = mutableMapOf<String, Boolean>()

    private var habitsListener: ValueEventListener? = null
    private var dailyListener: ValueEventListener? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        recycler = view.findViewById(R.id.habitRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = HabitAdapter(habits, completionMap) { habit, checked ->
            saveCompletion(habit.id, checked)
        }

        recycler.adapter = adapter

        val dateText = view.findViewById<TextView>(R.id.dateText)

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")

        dateText.text = today.format(formatter)


        loadHabits()
        loadTodayData()

        view.findViewById<MaterialButton>(R.id.manageHabitBtn)
            .setOnClickListener {
                startActivity(Intent(requireContext(), ManageHabitActivity::class.java))
            }

        return view
    }

    private fun todayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
    }

    //  Load only current user's habits (flat structure)
    private fun loadHabits() {

        habitsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                habits.clear()

                for (child in snapshot.children) {
                    val name = child.child("name")
                        .getValue(String::class.java)

                    if (name != null) {
                        habits.add(Habit(child.key!!, name))
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        database.child("Habits")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(habitsListener!!)
    }

    //  Load today's completion
    private fun loadTodayData() {

        dailyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                completionMap.clear()

                for (child in snapshot.children) {
                    val value = child.getValue(Boolean::class.java)
                    if (value != null) {
                        completionMap[child.key!!] = value
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        database.child("DailyHabitLogs")
            .child(userId)
            .child(todayDate())
            .addValueEventListener(dailyListener!!)
    }

    //  Save completion
    private fun saveCompletion(habitId: String, checked: Boolean) {

        database.child("DailyHabitLogs")
            .child(userId)
            .child(todayDate())
            .child(habitId)
            .setValue(checked)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        habitsListener?.let {
            database.child("Habits")
                .removeEventListener(it)
        }

        dailyListener?.let {
            database.child("DailyHabitLogs")
                .child(userId)
                .child(todayDate())
                .removeEventListener(it)
        }
    }
}

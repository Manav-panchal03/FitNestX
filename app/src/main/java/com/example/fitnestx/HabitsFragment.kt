package com.example.fitnestx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HabitsFragment : Fragment() {

    private lateinit var adapter: HabitAdapter
    private val habitList = mutableListOf<Habit>()
    private val progressMap = mutableMapOf<String, Int>()
    private lateinit var todayDate: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val rvHabits = view.findViewById<RecyclerView>(R.id.rvHabits)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddHabit)

        rvHabits.layoutManager = LinearLayoutManager(requireContext())
        adapter = HabitAdapter(habitList, progressMap, { habit -> incrementHabit(habit)},
            { habit ->
                showDeleteConfirmation(habit)
        } )
        rvHabits.adapter = adapter

        fetchHabitsAndProgress()

        fab.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun fetchHabitsAndProgress() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance()

        // 1. Listen for Habit Definitions
        db.getReference("Habits").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                habitList.clear()
                snapshot.children.forEach { it.getValue(Habit::class.java)?.let { h -> habitList.add(h) } }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Listen for Today's Progress
        db.getReference("HabitLogs").child(uid).child(todayDate).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressMap.clear()
                snapshot.children.forEach { progressMap[it.key!!] = it.child("currentProgress").getValue(Int::class.java) ?: 0 }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun incrementHabit(habit: Habit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("HabitLogs")
            .child(uid).child(todayDate).child(habit.id)

        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                // 1. Get current progress (default to 0 if null)
                val current = mutableData.child("currentProgress").getValue(Int::class.java) ?: 0

                // 2. CHECK: Only increment if we haven't reached the goal
                if (current < habit.goalValue) {
                    val newProgress = current + 1
                    mutableData.child("currentProgress").value = newProgress

                    // 3. Update completion status
                    mutableData.child("isCompleted").value = (newProgress >= habit.goalValue)

                    return Transaction.success(mutableData)
                } else {
                    // Goal already reached, do nothing
                    return Transaction.abort()
                }
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    // Optional: Play a "ding" sound or trigger haptic feedback
                }
            }
        })
    }
    private fun showDeleteConfirmation(habit: Habit) {
        // We use AlertDialog.Builder to get the user's permission before deleting
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'? This will remove all your progress history for this habit.")
            .setPositiveButton("Delete") { _, _ ->
                deleteHabitFromFirebase(habit)
            }
            .setNegativeButton("Cancel", null) // Does nothing, just closes the dialog
            .show()
    }

    private fun deleteHabitFromFirebase(habit: Habit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance()

        // 1. Delete the habit definition (This removes it from the list)
        db.getReference("Habits").child(uid).child(habit.id).removeValue()
            .addOnSuccessListener {

                // 2. Clean up: Delete logs for this habit from "HabitLogs"
                // We search through the logs and remove the specific habit ID entry
                val logsRef = db.getReference("HabitLogs").child(uid)

                logsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dateSnapshot in snapshot.children) {
                            if (dateSnapshot.hasChild(habit.id)) {
                                // Removes the progress data for this habit on every date it was tracked
                                dateSnapshot.child(habit.id).ref.removeValue()
                            }
                        }
                        Toast.makeText(context, "${habit.name} deleted successfully", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showAddHabitDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        dialog.setContentView(view)

        val etName = view.findViewById<android.widget.EditText>(R.id.etHabitName)
        val etGoal = view.findViewById<android.widget.EditText>(R.id.etGoalValue)
        val etUnit = view.findViewById<android.widget.EditText>(R.id.etUnit)
        val btnSave = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveHabit)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val goalStr = etGoal.text.toString().trim()
            val unit = etUnit.text.toString().trim()

            if (name.isEmpty() || goalStr.isEmpty() || unit.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val ref = FirebaseDatabase.getInstance().getReference("Habits").child(uid)
            val habitId = ref.push().key ?: return@setOnClickListener

            val newHabit = Habit(
                id = habitId,
                name = name,
                goalValue = goalStr.toInt(),
                unit = unit
            )

            ref.child(habitId).setValue(newHabit).addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(context, "Habit added!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
}
package com.example.fitnestx

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ManageHabitActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ManageHabitAdapter

    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val habits = mutableListOf<Habit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_habit)

        recycler = findViewById(R.id.manageRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = ManageHabitAdapter(
            habits,
            onEdit = { showEditDialog(it) },
            onDelete = { deleteHabit(it) }
        )

        recycler.adapter = adapter

        findViewById<FloatingActionButton>(R.id.addHabitFab)
            .setOnClickListener { showAddDialog() }

        loadHabits()
    }

    //  Load current user's habits
    private fun loadHabits() {

        database.child("Habits")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {

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
            })
    }

    // 🔥 Add Habit
    private fun showAddDialog() {

        val input = EditText(this)
        input.hint = "Habit name"

        AlertDialog.Builder(this)
            .setTitle("Add Habit")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->

                val name = input.text.toString()

                if (name.isNotEmpty()) {
                    val habitRef = database.child("Habits").push()

                    habitRef.child("name").setValue(name)
                    habitRef.child("userId").setValue(userId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //  Edit Habit
    private fun showEditDialog(habit: Habit) {

        val input = EditText(this)
        input.setText(habit.name)

        AlertDialog.Builder(this)
            .setTitle("Edit Habit")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->

                val newName = input.text.toString()

                database.child("Habits")
                    .child(habit.id)
                    .child("name")
                    .setValue(newName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //  Delete Habit
    private fun deleteHabit(habit: Habit) {

        database.child("Habits")
            .child(habit.id)
            .removeValue()
    }
}

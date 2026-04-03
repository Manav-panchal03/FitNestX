package com.example.fitnestx

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class WorkoutSessionActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: WorkoutSessionAdapter
    private lateinit var btnFinish: MaterialButton

    private val sessionSets = mutableListOf<WorkoutSetSession>()
    private lateinit var database: DatabaseReference
    private var isAlreadyDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_session)

        recycler = findViewById(R.id.rvWorkout)
        btnFinish = findViewById(R.id.btnComplete)

        val routine = intent.getParcelableExtra<RoutineModel>("ROUTINE_DATA") ?: return
        title = routine.routineName

// એક્સરસાઇઝ મુજબ સેટ્સ એડ કરવાનું લોજિક
        for (ex in routine.exercises) {
            var setCounter = 1 // ⭐ દરેક નવી એક્સરસાઇઝ માટે કાઉન્ટર ૧ થી શરૂ થશે
            for (set in ex.sets) {
                sessionSets.add(
                    WorkoutSetSession(
                        exercise = ex.name,
                        setNumber = setCounter, // ✅ હવે 'setNumber' પાસ થશે
                        plannedReps = set.reps.toIntOrNull() ?: 0,
                        plannedWeight = set.weight.toFloatOrNull() ?: 0f
                    )
                )
                setCounter++ // ⭐ આગલા સેટ માટે નંબર વધારો
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutSessionAdapter(sessionSets)
        recycler.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance().getReference("WorkoutSessions").child(userId)

        checkIfWorkoutDoneToday()

        btnFinish.setOnClickListener {
            if (isAlreadyDone) {
                showAlreadyDoneDialog()
            } else {
                saveWorkout(routine.routineName)
            }
        }
    }

    private fun checkIfWorkoutDoneToday() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val timestamp = ds.child("timestamp").getValue(Long::class.java) ?: 0L
                    val sessionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
                    if (sessionDate == todayDate) {
                        isAlreadyDone = true
                        btnFinish.text = "Workout Finished ✅"
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showAlreadyDoneDialog() {
        AlertDialog.Builder(this)
            .setTitle("Mission Accomplished! 🌟")
            .setMessage("You have already completed your workout for today. Great job!")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun saveWorkout(routineName: String) {
        val incompleteSets = sessionSets.filter { !it.completed }
        if (incompleteSets.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Incomplete Workout")
                .setMessage("You have ${incompleteSets.size} sets left. Do you want to save anyway?")
                .setPositiveButton("Yes, Save") { _, _ -> performSave(routineName) }
                .setNegativeButton("No", null)
                .show()
        } else {
            performSave(routineName)
        }
    }

    private fun performSave(routineName: String) {
        val completedSets = sessionSets.filter { it.completed }

        if (completedSets.isEmpty()) {
            Toast.makeText(this, "Atleast Do One Set! 💪", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐ ડેટાને Exercise પ્રમાણે Group કરવાનું Logic
        val groupedByExercise = completedSets.groupBy { it.exercise }
            .map { (exerciseName, sets) ->
                ExerciseSession(
                    exerciseName = exerciseName,
                    sets = sets.toMutableList()
                )
            }
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return // ⭐ આ લાઈન ઉમેરો
        val sessionId = database.push().key ?: ""
        val session = WorkoutSession(
            id = sessionId,
            routineName = routineName,
            timestamp = System.currentTimeMillis(),
            exercises = groupedByExercise // હવે અહીં ગ્રુપ કરેલી Exercise જશે
        )

        database.child(sessionId).setValue(session)
            .addOnSuccessListener {
                // ⭐ હવે અહીં 'userId' મળી જશે એટલે એરર જતી રહેશે
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val routine = intent.getParcelableExtra<RoutineModel>("ROUTINE_DATA")
                val routineId = routine?.id ?: ""

                if (routineId.isNotEmpty()) {
                    FirebaseDatabase.getInstance().getReference("Routines")
                        .child(userId) // ✅ હવે આ 'userId' વેલિડ છે
                        .child(routineId)
                        .child("lastCompletedDate")
                        .setValue(todayDate)
                }

                Toast.makeText(this, "Workout Saved! 🔥", Toast.LENGTH_SHORT).show()
                isAlreadyDone = true
                btnFinish.text = "Workout Finished ✅"
                showAlreadyDoneDialog()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
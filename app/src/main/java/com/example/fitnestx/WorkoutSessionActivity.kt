package com.example.fitnestx

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnestx.model.WorkoutLog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class WorkoutSessionActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: WorkoutSessionAdapter
    private lateinit var btnFinish: MaterialButton

    private val sessionSets = mutableListOf<WorkoutSetSession>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_session)

        recycler = findViewById(R.id.rvWorkout)
        btnFinish = findViewById(R.id.btnComplete)

        val routine = intent.getParcelableExtra<RoutineModel>("ROUTINE_DATA")!!

        title = routine.routineName

        // 🔥 Convert routine → session
        for (ex in routine.exercises) {
            for (set in ex.sets) {

                sessionSets.add(
                    WorkoutSetSession(
                        exercise = ex.name,
                        plannedReps = set.reps.toIntOrNull() ?: 0,
                        plannedWeight = set.weight.toFloatOrNull() ?: 0f
                    )
                )
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutSessionAdapter(sessionSets)
        recycler.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance()
            .getReference("WorkoutSessions")
            .child(userId)

        btnFinish.setOnClickListener { saveWorkout(routine.routineName) }
    }

    private fun saveWorkout(routineName: String) {

        val session = WorkoutSession(
            routineName = routineName,
            sets = sessionSets
        )

        database.push().setValue(session)

        Toast.makeText(this, "Workout Saved 💪", Toast.LENGTH_SHORT).show()
        finish()
    }
}

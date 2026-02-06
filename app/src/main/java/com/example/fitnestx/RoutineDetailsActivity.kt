package com.example.fitnestx

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RoutineDetailsActivity : AppCompatActivity() {

    private lateinit var routine: RoutineModel
    private lateinit var adapter : SelectedExerciseAdapter

    // edit screen per thi pachu avtah data refresh karo
    private val editRoutineLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadRoutineFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_routine_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. get data from intent
        routine = intent.getParcelableExtra("ROUTINE_DATA") ?: return

        findViewById<TextView>(R.id.tvDetailRoutineName).text = routine.routineName

        // 2. setup recycler view (using a Create walo adapter but only for read only mode)
        val rv = findViewById<RecyclerView>(R.id.rvExerciseDetails)
        rv.layoutManager = LinearLayoutManager(this)


        adapter = SelectedExerciseAdapter(routine.exercises.toMutableList() , isReadOnly = true)
        rv.adapter = adapter

        // hevy style swipe logic start ----------------------- //

        // 3. edit button click
        findViewById<ImageButton>(R.id.btnEditRoutine).setOnClickListener {
            val intent = Intent(this, CreateWorkOutActivity::class.java)
            intent.putExtra("EDIT_ROUTINE", routine)
            editRoutineLauncher.launch(intent)

        }
    }

    //  Firebase thi latest data load function
    private fun loadRoutineFromFirebase() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val routineId = routine.id ?: return   // ensure tamara model ma id che

        val ref = FirebaseDatabase.getInstance()
            .getReference("Routines")
            .child(userId)
            .child(routineId)

        ref.get().addOnSuccessListener { snapshot ->

            val updatedRoutine =
                snapshot.getValue(RoutineModel::class.java)

            if (updatedRoutine != null) {

                routine = updatedRoutine

                // update title
                findViewById<TextView>(R.id.tvDetailRoutineName).text =
                    routine.routineName

                // update recycler data
                adapter.updateList(
                    routine.exercises.toMutableList()
                )
            }
        }
    }
}
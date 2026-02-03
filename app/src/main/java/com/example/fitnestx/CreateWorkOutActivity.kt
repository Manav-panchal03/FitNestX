package com.example.fitnestx

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateWorkOutActivity : AppCompatActivity() {
    private var selectedList = mutableListOf<LogExercise>()
    private lateinit var adapter: SelectedExerciseAdapter
    private lateinit var rvSelectedExercises: RecyclerView
    private lateinit var etRoutine: EditText


    // 1. Fixed Launcher
    private val getExercises = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getParcelableArrayListExtra<ExerciseModel>("Selected_list")

            data?.forEach { model ->
                if (!selectedList.any { it.name == model.name }) {
                    // Fix: model.images mathi paheli image lo, ane target ni jagya e bodyPart lo
                    val imageUrl = if (!model.images.isNullOrEmpty()) model.images[0] else ""
                    val targetMuscle = if (!model.primaryMuscles.isNullOrEmpty()) model.primaryMuscles[0] else model.bodyPart ?: ""

                    selectedList.add(
                        LogExercise(
                            name = model.name ?: "Unknown",
                            image = imageUrl,
                            target = targetMuscle
                        )
                    )
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_work_out)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Views Initialization
        etRoutine = findViewById(R.id.etRoutineName)
        rvSelectedExercises = findViewById(R.id.rvSelectedExercises)
        val btnAddExercises = findViewById<MaterialButton>(R.id.btnAddExercise)
        val btnSaveRoutine = findViewById<MaterialButton>(R.id.btnSaveRoutine)

        adapter = SelectedExerciseAdapter(selectedList)
        rvSelectedExercises.layoutManager = LinearLayoutManager(this)
        rvSelectedExercises.adapter = adapter

        btnAddExercises.setOnClickListener {
            val intent = Intent(this, ExerciseSearchActivity::class.java)
            getExercises.launch(intent)
        }

        btnSaveRoutine.setOnClickListener {
            // Save logic
            // Toast.makeText(this, "Routine Saved!", Toast.LENGTH_SHORT).show()

            val routinename =etRoutine.text.toString().trim()

            if(routinename.isEmpty()){
                etRoutine.error = "Please enter a routine name"
                return@setOnClickListener
            }

            if(selectedList.isEmpty()){
                Toast.makeText(this, "Please add at least one exercise!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveRoutineToFirebase(routinename)
        }
    }

    private fun saveRoutineToFirebase(routineName : String){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if(userId == null){
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show()
            return
        }

        // set the reference
        val databaseRef = FirebaseDatabase.getInstance().getReference("Routines").child(userId)
        val routineId = databaseRef.push().key ?: " "

        //create a model
        val newRoutine = RoutineModel(
            id = routineId,
            routineName = routineName,
            exercises = selectedList,
            createdAt = System.currentTimeMillis()
        )

        // store data in firebase
        databaseRef.child(routineId).setValue(newRoutine)
            .addOnSuccessListener {
                Toast.makeText(this, "Routine '$routineName' saved! 🦾", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}
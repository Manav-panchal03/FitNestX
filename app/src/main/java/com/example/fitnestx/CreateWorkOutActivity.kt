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

//        val btnAddExercises = findViewById<MaterialButton>(R.id.btnAddExercise)

        // Adapter Setup
                adapter = SelectedExerciseAdapter(selectedList)
        rvSelectedExercises.layoutManager = LinearLayoutManager(this)
        rvSelectedExercises.adapter = adapter

        btnAddExercises.setOnClickListener {
            val intent = Intent(this, ExerciseSearchActivity::class.java)
            getExercises.launch(intent)
        }

        btnSaveRoutine.setOnClickListener {
            // Save logic
            Toast.makeText(this, "Routine Saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
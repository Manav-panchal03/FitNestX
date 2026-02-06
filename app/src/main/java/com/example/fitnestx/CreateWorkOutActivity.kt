package com.example.fitnestx

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
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

    // for handle edit mode
    private var isEditMode = false
    private var routineIdToEdit: String? = null


    // 1. Fixed Launcher
    private val getExercises = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getParcelableArrayListExtra<ExerciseModel>("Selected_list")

            data?.forEach { model ->
                if (!selectedList.any { it.name == model.name }) {
                    // Fix: model.images mathi paheli image lo, ane target ni jagya e bodyPart lo
                    val imageUrl = if (!model.images.isNullOrEmpty()) model.images[0] else ""
                    val targetMuscle = if (!model.primaryMuscles.isNullOrEmpty()) model.primaryMuscles[0] else model.bodyPart ?: ""

                    val defualtSets = mutableListOf<ExerciseSet>(
                        ExerciseSet(setNumber = 1 , weight = "" , reps = "")
                    )

                    selectedList.add(
                        LogExercise(
                            name = model.name ?: "Unknown",
                            image = imageUrl,
                            target = targetMuscle ,
                            sets = defualtSets
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
        val tvHeaderTitle = findViewById<TextView>(R.id.tvHeaderTitle) // Layout ma name check karjo

        // 1.  Edit Mode Check
        // detail screen mathi "EDIT_ROUTINE" pass kervama avse
        val exisingRoutine = intent.getParcelableExtra<RoutineModel>("EDIT_ROUTINE")
        if (exisingRoutine != null){
            isEditMode = true
            routineIdToEdit = exisingRoutine.id
            etRoutine.setText(exisingRoutine.routineName) //Routine name set thase
            selectedList.clear()
            selectedList.addAll(exisingRoutine.exercises) // purani exercises load karo
            btnSaveRoutine.text = "Update Routine"
            tvHeaderTitle.text = "Edit Routine"
        }

        // 2. adapter setup
        adapter = SelectedExerciseAdapter(selectedList , false)
        rvSelectedExercises.layoutManager = LinearLayoutManager(this)
        rvSelectedExercises.adapter = adapter

        btnAddExercises.setOnClickListener {
            val intent = Intent(this, ExerciseSearchActivity::class.java)
            getExercises.launch(intent)
        }

        btnSaveRoutine.setOnClickListener {
            // Save logic
            val routinename =etRoutine.text.toString().trim()

            // 1. validate routine name
            if(routinename.isEmpty()){
                etRoutine.error = "Please enter a routine name"
                etRoutine.requestFocus()
                return@setOnClickListener
            }

            // 2. validate if at least one exercise is selected
            if(selectedList.isEmpty()){
                Toast.makeText(this, "Please add at least one exercise!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. validation for every reps and weight for each exercises
            var isValid = true
            for (exercises in selectedList){
                for(set in exercises.sets){
                    if(set.weight.trim().isEmpty() || set.reps.trim().isEmpty()){
                        isValid = false
                        break
                    }
                }
                if(!isValid){
                    Toast.makeText(this, "Please fill Weight & Reps for ${exercises.name}", Toast.LENGTH_SHORT).show()
                    break
                }
            }

            // only save if everything is valid
            if(isValid){
                saveRoutineToFirebase(routinename)
            }
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
        val routineId =  if(isEditMode) routineIdToEdit!! else databaseRef.push().key ?: return

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
                val message = if(isEditMode) "Routine '$routineName' updated!" else "Routine '$routineName' saved!"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}
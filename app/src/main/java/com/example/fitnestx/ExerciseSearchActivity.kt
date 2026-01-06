package com.example.fitnestx

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExerciseSearchActivity : AppCompatActivity() {

    private lateinit var adapter: ExerciseAdapter
    private var fullExerciseList = mutableListOf<ExerciseModel>()
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddSelected: MaterialButton // Havy app jevu Add Button
    private lateinit var searchView: androidx.appcompat.widget.SearchView

    // API ma available badha body parts/muscles
    private val muscleGroups = listOf(
        "Abdominals", "Hamstrings", "Adductors", "Chest", "Back",
        "Biceps", "Triceps", "Quads", "Shoulders", "Glutes", "Calves"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exercise_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rvExerciseAPI)
        recyclerView.layoutManager = LinearLayoutManager(this)

        progressBar = findViewById(R.id.progressBar)
        btnAddSelected = findViewById(R.id.btnAddExercises) // XML ma aa ID nu button banavjo

        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        searchView.isFocusable = true
        searchView.isIconified = false
        searchView.requestFocusFromTouch()

        // 1. API mathi data load karo
        loadExercisesFromAPI()
        setUpMuscleChips()

        // 2. Search logic
        // ... searchView logic ni andar update karo ...
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        // 3. "Add" Button Logic (Havy app reference)
        btnAddSelected.setOnClickListener {
            if (::adapter.isInitialized) {
                val selectedList = adapter.getSelectedExercises()
                if (selectedList.isNotEmpty()) {
                    // Have ahiya tame aa list ne pacha activity ma mokli sako ya Firebase ma save karo
                    Toast.makeText(this, "${selectedList.size} Exercises Added!", Toast.LENGTH_SHORT).show()

                    // TODO: Save to Firebase or Return to Previous Screen
                    finish()
                } else {
                    Toast.makeText(this, "Select at least one exercise!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setUpMuscleChips(){
        val chipGroup = findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupMuscles)
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

        muscleGroups.forEach { muscleName ->
            val chip = Chip(this).apply {
                text = muscleName
                isCheckable = false
                isClickable = true
                // Modern UI mate stroke ane background
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F5F5F5"))
                chipStrokeWidth = 2f
                chipStrokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E0"))
            }

            chip.setOnClickListener {
                searchView.setQuery(muscleName , true)
            }
            chipGroup.addView(chip)

        }

    }

    private fun filterList(newText: String?) {
        if (::adapter.isInitialized) {
            val query = newText?.lowercase() ?: ""
            if (query.isEmpty()) {
                adapter.updateList(fullExerciseList)
            } else {
                val filtered = fullExerciseList.filter { exercise ->
                    val nameMatch = exercise.name?.lowercase()?.contains(query) == true
                    val muscleMatch = exercise.primaryMuscles?.any { it.lowercase().contains(query) } == true
                    nameMatch || muscleMatch
                }
                adapter.updateList(filtered.toMutableList())
            }
        }
    }

    private fun loadExercisesFromAPI() {
        progressBar.visibility = View.VISIBLE
        // Have apde headers vagar ni simple API call kariye chiye (GitHub vali)
        RetrofitInstance.api.getAllExercises().enqueue(object : Callback<List<ExerciseModel>> {
            override fun onResponse(call: Call<List<ExerciseModel>>, response: Response<List<ExerciseModel>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    fullExerciseList = response.body()?.toMutableList() ?: mutableListOf()
                    adapter = ExerciseAdapter(fullExerciseList){selectedCount ->
                        if (selectedCount > 0) {
                            btnAddSelected.visibility = View.VISIBLE
                            btnAddSelected.text = "Add $selectedCount Exercises"
                        } else {
                            btnAddSelected.text = "Add Exercises"
                            // Tame chaho to hide pan kari sako: btnAddSelected.visibility = View.GONE
                        }
                    }
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@ExerciseSearchActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ExerciseModel>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ExerciseSearchActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
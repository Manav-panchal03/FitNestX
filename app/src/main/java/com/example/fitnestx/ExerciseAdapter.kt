package com.example.fitnestx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExerciseAdapter(private var exerciseList: MutableList<ExerciseModel>) :
    RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    private var filteredList: MutableList<ExerciseModel> = exerciseList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvExerciseName)
        val gif: ImageView = view.findViewById(R.id.ivExerciseGif)
        val checkBox: CheckBox = view.findViewById(R.id.cbSelect)
        val target: TextView = view.findViewById(R.id.tvTarget)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_api, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= filteredList.size) return
        val exercise = filteredList[position]

        holder.name.text = exercise.name?.replaceFirstChar { it.uppercase() } ?: "Unknown Exercise"

        // 1. Target logic: primaryMuscles List che, etle pehli item uthavo
        val muscle = exercise.primaryMuscles?.firstOrNull() ?: "Unknown"
        holder.target.text = "Target: ${muscle.replaceFirstChar { it.uppercase() }}"

        // 2. Image Logic: GitHub ma images/ folder ma 0.jpg che
        // Base URL mate RetrofitInstance valo BASE_URL + images[0]
        val imageUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/${exercise.images?.firstOrNull()}"

        Glide.with(holder.itemView.context)
            .load(imageUrl) // Ahiya .asGif() kadhi nakhyu che kem ke a JPG che
            .placeholder(android.R.drawable.progress_horizontal)
            .error(R.drawable.ic_launcher_background) // Tamara project ma jo error image hoy to
            .into(holder.gif)

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = exercise.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            exercise.isSelected = isChecked
            exerciseList.find { it.name == exercise.name }?.isSelected = isChecked
        }
    }

    override fun getItemCount() = filteredList.size

    fun updateList(newList: List<ExerciseModel>) {
        filteredList.clear()
        filteredList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getSelectedExercises(): List<ExerciseModel> {
        return exerciseList.filter { it.isSelected }
    }
}
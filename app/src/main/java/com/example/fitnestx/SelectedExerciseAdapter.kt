package com.example.fitnestx

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class SelectedExerciseAdapter(private val selectedExercises: MutableList<LogExercise>)
    : RecyclerView.Adapter<SelectedExerciseAdapter.ViewHolder>() {
    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val tvName = view.findViewById<TextView>(R.id.tvExerciseName)
        val rvSets = view.findViewById<RecyclerView>(R.id.rvSets)
        val btnAddSet = view.findViewById<MaterialButton>(R.id.btnAddSet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_exercise , parent , false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = selectedExercises[position]
        holder.tvName.text = exercise.name

        holder.itemView.setOnLongClickListener {
            // Ek confirmation dialog batavvo hamesha saru rahese
            showDeleteConfirmation(holder.itemView.context, holder.adapterPosition)
            true
        }

        //nested adapter for sets
        val setAdapter = SetAdapter(exercise.sets)
        holder.rvSets.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvSets.adapter = setAdapter

        holder.btnAddSet.setOnClickListener {
            exercise.sets.add(ExerciseSet(exercise.sets.size + 1))
            setAdapter.notifyItemInserted(exercise.sets.size - 1)
        }
    }

    override fun getItemCount() = selectedExercises.size

    private fun showDeleteConfirmation(context: Context, position: Int){
        android.app.AlertDialog.Builder(context)
            .setTitle("Remove Exercise")
            .setMessage("Are you sure you want to remove this exercise from your routine?")
            .setPositiveButton("Remove") { _, _ ->
                selectedExercises.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, selectedExercises.size)
                Toast.makeText(context, "Exercise removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}


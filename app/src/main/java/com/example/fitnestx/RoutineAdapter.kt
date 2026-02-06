package com.example.fitnestx

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class RoutineAdapter(
    private val routines: List<RoutineModel>,
    private val onStartClick: (RoutineModel) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRoutineName)
        val tvSummary: TextView = view.findViewById(R.id.tvExerciseSummary)
        val btnStart: MaterialButton = view.findViewById(R.id.btnStartRoutine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val routine = routines[position]
        holder.tvName.text = routine.routineName

        // Exercise summary (e.g. "Bench Press, Squat...")
        val summary = routine.exercises.joinToString(", ") { it.name }
        holder.tvSummary.text = if (summary.length > 60) summary.take(60) + "..." else summary

        holder.btnStart.setOnClickListener { onStartClick(routine) }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RoutineDetailsActivity::class.java)
            intent.putExtra("ROUTINE_DATA", routine)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount() = routines.size
}
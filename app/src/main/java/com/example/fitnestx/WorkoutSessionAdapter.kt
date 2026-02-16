package com.example.fitnestx

import android.R.attr.checked
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutSessionAdapter(
    private val sets: MutableList<WorkoutSetSession>
) : RecyclerView.Adapter<WorkoutSessionAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvExercise: TextView = v.findViewById(R.id.tvExercise)
        val etReps: EditText = v.findViewById(R.id.etReps)
        val etWeight: EditText = v.findViewById(R.id.etWeight)
        val check: CheckBox = v.findViewById(R.id.checkDone)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        return VH(
            LayoutInflater.from(p.context)
                .inflate(R.layout.item_workout_set, p, false)
        )
    }

    override fun onBindViewHolder(h: VH, i: Int) {

        val set = sets[i]

        h.tvExercise.text =
            "${set.exercise} (${set.plannedReps} reps)"

        h.etReps.setText(set.plannedReps.toString())
        h.etWeight.setText(set.plannedWeight.toString())

        h.check.setOnCheckedChangeListener { _, checked ->

            set.completed = checked
            set.actualReps =
                h.etReps.text.toString().toIntOrNull() ?: 0
            set.actualWeight =
                h.etWeight.text.toString().toFloatOrNull() ?: 0f
        }

        h.check.setOnClickListener {
            h.itemView.animate()
                .alpha(if(h.check.isChecked) 0.6f else 1f)
                .setDuration(150)
                .start()
        }
    }

    override fun getItemCount() = sets.size
}

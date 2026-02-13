package com.example.fitnestx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class HabitAdapter(
    private val habits: List<Habit>,
    private val progressMap: Map<String, Int>,
    private val onIncrement: (Habit) -> Unit,
    private val onLongClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvHabitName)
        val tvProgress: TextView = view.findViewById(R.id.tvHabitProgress)
        val progressBar: ProgressBar = view.findViewById(R.id.habitProgressBar)
        val btnAdd: MaterialButton = view.findViewById(R.id.btnIncrement)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habit = habits[position]
        // Get progress from the map, default to 0 if no log exists for today
        val currentProgress = progressMap[habit.id] ?: 0

        // 1. Set Texts
        holder.tvName.text = habit.name
        holder.tvProgress.text = "$currentProgress / ${habit.goalValue} ${habit.unit}"

        // 2. Calculate and Set Progress
        // Formula: (Current / Goal) * 100
        val progressPercent = if (habit.goalValue > 0) {
            (currentProgress.toFloat() / habit.goalValue.toFloat() * 100).toInt()
        } else 0

        holder.progressBar.progress = progressPercent

        // 3. Increment Button Logic
        holder.btnAdd.setOnClickListener {
            onIncrement(habit)
        }

        // 4. Delete Logic (Long Click)
        holder.itemView.setOnLongClickListener {
            onLongClick(habit)
            true // consumes the click
        }

        // 5. Visual Feedback if completed
        if (currentProgress >= habit.goalValue) {
            holder.btnAdd.isEnabled = false
            holder.btnAdd.alpha = 0.5f // Make it look disabled
        } else {
            holder.btnAdd.isEnabled = true
            holder.btnAdd.alpha = 1.0f
        }
    }

    override fun getItemCount() = habits.size
}
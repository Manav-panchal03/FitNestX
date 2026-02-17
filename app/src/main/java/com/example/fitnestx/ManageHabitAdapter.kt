package com.example.fitnestx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ManageHabitAdapter(
    private val habits: MutableList<Habit>,
    private val onEdit: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<ManageHabitAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.habitName)
        val edit: ImageButton = view.findViewById(R.id.editBtn)
        val delete: ImageButton = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_habit, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = habits.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habit = habits[position]
        holder.name.text = habit.name

        holder.edit.setOnClickListener { onEdit(habit) }
        holder.delete.setOnClickListener { onDelete(habit) }
    }
}

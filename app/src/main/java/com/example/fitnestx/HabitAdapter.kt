package com.example.fitnestx

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val completionMap: MutableMap<String, Boolean>,
    private val onChecked: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private var showChecked = true

    init {
        setHasStableIds(true)
    }

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.habitName)
        val check: CheckBox = view.findViewById(R.id.habitCheck)
    }

    override fun getItemId(position: Int): Long {
        return habits[position].id.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (showChecked) habits.size
        else habits.count { completionMap[it.id] != true }
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val visibleList = if (showChecked) habits else habits.filter { completionMap[it.id] != true }
        val habit = visibleList[position]
        val isChecked = completionMap[habit.id] ?: false

        holder.name.text = habit.name
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = isChecked

        // --- IMPORTANT: Reset View State ---
        holder.itemView.translationY = 0f
        holder.itemView.alpha = 1f
        holder.itemView.scaleX = 1f
        holder.itemView.scaleY = 1f

        applyStyle(holder, isChecked)

        holder.check.setOnClickListener {
            val checked = holder.check.isChecked

            // 1. Google Keep Style Animation
            holder.itemView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .alpha(0.5f)
                .setDuration(200)
                .withEndAction {
                    // 2. Data update
                    completionMap[habit.id] = checked
                    onChecked(habit, checked)

                    // 3. Move logic with delay to let animation finish
                    val fromPos = habits.indexOf(habit)
                    if (fromPos != -1) {
                        habits.removeAt(fromPos)
                        val toPos = if (checked) habits.size else 0
                        habits.add(toPos, habit)

                        // Move and then refresh to reset scale
                        notifyItemMoved(fromPos, toPos)

                        // Aa delay size fix karse!
                        holder.itemView.postDelayed({
                            notifyItemChanged(toPos)
                        }, 300)
                    }
                }
                .start()
        }
    }

    private fun applyStyle(holder: HabitViewHolder, checked: Boolean) {
        if (checked) {
            holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.name.alpha = 0.5f
        } else {
            holder.name.paintFlags = holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.name.alpha = 1f
        }
    }


    fun toggleCheckedVisibility(show: Boolean) {
        showChecked = show
        notifyDataSetChanged()
    }
}
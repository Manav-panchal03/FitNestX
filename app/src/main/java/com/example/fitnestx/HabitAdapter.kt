package com.example.fitnestx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private val habits: List<Habit>,
    private val completionMap: MutableMap<String, Boolean>,
    private val onChecked: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.habitName)
        val check: CheckBox = view.findViewById(R.id.habitCheck)
        val card: View = view.findViewById(R.id.habitCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {

        val habit = habits[position]

        holder.name.text = habit.name

        // ❗ remove old listener first
        holder.check.setOnCheckedChangeListener(null)

        holder.check.isChecked = completionMap[habit.id] ?: false

        // ✅ click listener instead of change listener
        holder.check.setOnClickListener {

            val checked = holder.check.isChecked

            animateCard(holder.card)

            completionMap[habit.id] = checked
            onChecked(habit, checked)
        }
    }

    override fun getItemCount() = habits.size


    // ✅ STRONG visible animation
    private fun animateCard(view: View) {

        view.animate().cancel()

        view.scaleX = 1f
        view.scaleY = 1f

        view.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(100)
            .withEndAction {

                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
}

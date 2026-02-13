package com.example.fitnestx

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference

class RoutineAdapter(
    private val routines: MutableList<RoutineModel>,
    private val onStartClick: (RoutineModel) -> Unit,
    private val dbRef: DatabaseReference
) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRoutineName)
        val tvSummary: TextView = view.findViewById(R.id.tvExerciseSummary)
        val btnStart: MaterialButton = view.findViewById(R.id.btnStartRoutine)
        val btnMenu: ImageButton = view.findViewById(R.id.btnMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val routine = routines[position]

        holder.tvName.text = routine.routineName

        val summary = routine.exercises.joinToString(", ") { it.name }
        holder.tvSummary.text =
            if (summary.length > 60) summary.take(60) + "..." else summary

        holder.btnStart.setOnClickListener {
            onStartClick(routine)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,
                RoutineDetailsActivity::class.java)
            intent.putExtra("ROUTINE_DATA", routine)
            holder.itemView.context.startActivity(intent)
        }

        holder.btnMenu.setOnClickListener { view ->

            val popup = PopupMenu(view.context, view)

            popup.menuInflater.inflate(R.menu.menu_routine_item, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_delete -> {
                        showDeleteDialog(holder, position)
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }

    }

    override fun getItemCount() = routines.size

    private fun showDeleteDialog(holder: ViewHolder, position: Int) {

        AlertDialog.Builder(holder.itemView.context)
            .setTitle("Delete Routine")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->

                val routine = routines[position]

                holder.itemView.animate()
                    .translationX(holder.itemView.width.toFloat())
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction {

                        dbRef.child(routine.id).removeValue()

                        routines.removeAt(position)
                        notifyItemRemoved(position)
                    }

            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

package com.example.fitnestx

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*

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

        // Exercise summary logic
        val summary = routine.exercises.joinToString(", ") { it.name }
        holder.tvSummary.text = if (summary.length > 60) summary.take(60) + "..." else summary

        // --- ⭐ DATE CHECK LOGIC STARTS HERE ---

        // 1. આજની તારીખ મેળવો (Format: yyyy-MM-dd)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 2. ચેક કરો કે આ રૂટિન આજે પૂરું થયું છે?
        if (routine.lastCompletedDate.trim() == todayDate.trim()) {
            // જો આજે પૂરું થઈ ગયું હોય
            holder.btnStart.text = "Completed Today ✅"
            holder.btnStart.isEnabled = false // બટન Disable થશે
            holder.btnStart.alpha = 0.5f     // બટન ઝાંખું દેખાશે
            holder.btnStart.setTextColor(Color.BLACK)
            holder.btnStart.setBackgroundColor(Color.GRAY) // કલર બદલાઈ જશે
        } else {
            // જો બાકી હોય તો નોર્મલ બટન
            holder.btnStart.text = "Start Routine"
            holder.btnStart.isEnabled = true
            holder.btnStart.alpha = 1.0f
            holder.btnStart.setBackgroundColor(Color.parseColor("#008DFF")) // તારો ઓરિજિનલ કલર
        }

        // --- ⭐ DATE CHECK LOGIC ENDS HERE ---

        holder.btnStart.setOnClickListener {
            onStartClick(routine)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RoutineDetailsActivity::class.java)
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
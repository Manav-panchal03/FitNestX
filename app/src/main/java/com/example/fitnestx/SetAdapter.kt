package com.example.fitnestx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class SetAdapter(private val sets: MutableList<ExerciseSet>) :
    RecyclerView.Adapter<SetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSetNum = view.findViewById<TextView>(R.id.tvSetNumber)
        val etWeight = view.findViewById<EditText>(R.id.etWeight)
        val etReps = view.findViewById<EditText>(R.id.etReps)
        val btnDeleteSets = view.findViewById<ImageButton>(R.id.btnDeleteSet)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetAdapter.ViewHolder, position: Int) {
        val set = sets[position]
        holder.tvSetNum.text = "${position + 1}"

        holder.btnDeleteSets.setOnClickListener {
            if(sets.size > 1){
                sets.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, sets.size)
            }
            else{
                Toast.makeText(holder.itemView.context, "Minimum 1 set is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = sets.size
}
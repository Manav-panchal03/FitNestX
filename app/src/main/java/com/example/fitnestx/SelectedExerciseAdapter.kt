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
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce


class SelectedExerciseAdapter(private val selectedExercises: MutableList<LogExercise> ,private val isReadOnly: Boolean = false)
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
        val setAdapter = SetAdapter(exercise.sets , isReadOnly)
        holder.rvSets.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvSets.adapter = setAdapter

        if(isReadOnly){
            holder.btnAddSet.visibility = View.GONE
            //long press delete
            holder.itemView.setOnLongClickListener { false }
        }
        else{
            // edit mode
            holder.btnAddSet.visibility = View.VISIBLE
            holder.itemView.setOnLongClickListener {
                showDeleteConfirmation(holder.itemView.context, holder.adapterPosition)
                true
            }
//            holder.btnAddSet.setOnClickListener {
//                exercise.sets.add(ExerciseSet(exercise.sets.size + 1))
//                setAdapter.notifyItemInserted(exercise.sets.size - 1)
//                holder.rvSets.scheduleLayoutAnimation()
//            }
            holder.btnAddSet.setOnClickListener {

                exercise.sets.add(
                    ExerciseSet(exercise.sets.size + 1)
                )

                val pos = exercise.sets.size - 1
                setAdapter.notifyItemInserted(pos)

                holder.rvSets.post {
                    val viewHolder =
                        holder.rvSets.findViewHolderForAdapterPosition(pos)

                    viewHolder?.itemView?.let { itemView ->

                        itemView.scaleX = 0.8f
                        itemView.scaleY = 0.8f

                        val springX = SpringAnimation(
                            itemView,
                            SpringAnimation.SCALE_X,
                            1f
                        )

                        val springY = SpringAnimation(
                            itemView,
                            SpringAnimation.SCALE_Y,
                            1f
                        )

                        springX.spring.stiffness =
                            SpringForce.STIFFNESS_MEDIUM

                        springY.spring.stiffness =
                            SpringForce.STIFFNESS_MEDIUM

                        springX.start()
                        springY.start()
                    }
                }
            }

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

    fun updateList(newList: MutableList<LogExercise>) {
        selectedExercises.clear()
        selectedExercises.addAll(newList)
        notifyDataSetChanged()
    }

}


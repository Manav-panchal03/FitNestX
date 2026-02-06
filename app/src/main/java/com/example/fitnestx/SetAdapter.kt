package com.example.fitnestx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import android.os.CountDownTimer



class SetAdapter(
    private val sets: MutableList<ExerciseSet>,
    private val isReadOnly: Boolean = false
) : RecyclerView.Adapter<SetAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) :
        RecyclerView.ViewHolder(view) {

        val tvSetNum: TextView =
            view.findViewById(R.id.tvSetNumber)

        val etWeight: EditText =
            view.findViewById(R.id.etWeight)

        val etReps: EditText =
            view.findViewById(R.id.etReps)

        val btnDelete: ImageButton =
            view.findViewById(R.id.btnDeleteSet)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_set_row, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val set = sets[position]

        holder.tvSetNum.text = "${position + 1}"
        holder.etWeight.setText(set.weight)
        holder.etReps.setText(set.reps)

        // READ ONLY MODE (FULL LOCK)
        if (isReadOnly) {

            holder.etWeight.isEnabled = false
            holder.etReps.isEnabled = false

            holder.etReps.setTextColor(android.graphics.Color.DKGRAY)
            holder.etWeight.setTextColor(android.graphics.Color.DKGRAY)

            holder.etWeight.isFocusable = false
            holder.etReps.isFocusable = false

            holder.etWeight.isClickable = false
            holder.etReps.isClickable = false

            holder.btnDelete.visibility = View.GONE
            // IMPORTANT: listeners attach na karo
            return
        }

        //  EDIT MODE ONLY BELOW

        holder.etReps.doAfterTextChanged {
            set.reps = it.toString()
        }

        holder.etWeight.doAfterTextChanged {
            set.weight = it.toString()
        }

        holder.btnDelete.visibility = android.view.View.VISIBLE

        holder.btnDelete.setOnClickListener {

            val currentPos = holder.bindingAdapterPosition

            if (currentPos != RecyclerView.NO_POSITION && sets.size > 1) {

                val deletedSet = sets[currentPos]

                // remove item
                sets.removeAt(currentPos)
                notifyItemRemoved(currentPos)

                val snackbar = Snackbar.make(
                    holder.itemView,
                    "Set deleted (5)",
                    Snackbar.LENGTH_INDEFINITE
                )

                snackbar.setAction("UNDO") {
                    sets.add(currentPos, deletedSet)
                    notifyItemInserted(currentPos)
                }

                snackbar.show()

                // countdown timer (5 sec)
                object : CountDownTimer(5000, 1000) {

                    override fun onTick(millisUntilFinished: Long) {

                        val secondsLeft =
                            (millisUntilFinished / 1000).toInt()

                        snackbar.setText(
                            "Set deleted ($secondsLeft)"
                        )
                    }

                    override fun onFinish() {
                        snackbar.dismiss()
                    }

                }.start()

            } else {

                Toast.makeText(
                    holder.itemView.context,
                    "At least one set is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



    }

    override fun getItemCount() = sets.size
}

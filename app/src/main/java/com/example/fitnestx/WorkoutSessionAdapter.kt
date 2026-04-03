package com.example.fitnestx

import android.text.Editable
import android.text.TextWatcher
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
        val tvSetNumber: TextView = v.findViewById(R.id.tvSetNumber) // નવું TextView ઉમેરજો
        val etReps: EditText = v.findViewById(R.id.etReps)
        val etWeight: EditText = v.findViewById(R.id.etWeight)
        val check: CheckBox = v.findViewById(R.id.checkDone)
    }

    override fun onCreateViewHolder(p: ViewGroup, i: Int): VH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_workout_set, p, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val set = sets[i]

        // 1. એક્સરસાઇઝનું નામ અને સેટ નંબર સેટ કરો
        h.tvExercise.text = set.exercise
        h.tvSetNumber.text = "Set ${set.setNumber}" // (તારા Model માં setNumber હોવો જોઈએ)

        // 2. જૂના લિસ્ટનર્સ હટાવો (Recycling bug ફિક્સ કરવા માટે)
        h.check.setOnCheckedChangeListener(null)

        // 3. ડેટા ડિસ્પ્લે કરો
        h.etReps.setText(if (set.actualReps > 0) set.actualReps.toString() else set.plannedReps.toString())
        h.etWeight.setText(if (set.actualWeight > 0f) set.actualWeight.toString() else set.plannedWeight.toString())
        h.check.isChecked = set.completed

        // 4. લુક અપડેટ (Alpha animation)
        h.itemView.alpha = if (set.completed) 0.6f else 1.0f

        // 5. Reps બદલાય ત્યારે તરત સેવ કરો (TextWatcher)
        h.etReps.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.actualReps = s.toString().toIntOrNull() ?: 0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 6. Weight બદલાય ત્યારે તરત સેવ કરો
        h.etWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.actualWeight = s.toString().toFloatOrNull() ?: 0f
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 7. ચેકબૉક્સ ક્લિક લોજિક
        h.check.setOnCheckedChangeListener { _, isChecked ->
            set.completed = isChecked
            h.itemView.animate().alpha(if (isChecked) 0.6f else 1.0f).setDuration(150).start()
        }
    }

    override fun getItemCount() = sets.size

    // Recycling વખતે ડેટા મિક્સ ના થાય તે માટે આ જરૂરી છે
    override fun getItemViewType(position: Int): Int = position
}
package com.example.fitnestx

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
class ExerciseAdapter(private var exerciseList: MutableList<ExerciseModel> , private val onSelectionChanged:(Int) -> Unit) :
    RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    private var filteredList: MutableList<ExerciseModel> = exerciseList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvExerciseName)
        val imageSwitcher: ImageSwitcher = view.findViewById(R.id.ivExerciseGif)
        val checkBox: CheckBox = view.findViewById(R.id.cbSelect)
        val target: TextView = view.findViewById(R.id.tvTarget)

        // Image switching handle karva mate references
        var imageHandler: Handler? = null
        var imageRunnable: Runnable? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_api, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= filteredList.size) return
        val exercise = filteredList[position]

        holder.name.text = exercise.name?.replaceFirstChar { it.uppercase() } ?: "Unknown Exercise"
        val muscle = exercise.primaryMuscles?.firstOrNull() ?: "Unknown"
        holder.target.text = "Target: ${muscle.replaceFirstChar { it.uppercase() }}"

        // --- IMAGE SWITCHER LOGIC ---

        // 1. Juna handlers ne clear karo jethi image mix na thay
        holder.imageHandler?.removeCallbacksAndMessages(null)

        // 1.5 . Setup ImageSwitcher Factory (If not already set)
        if (holder.imageSwitcher.childCount == 0) {
            holder.imageSwitcher.setFactory {
                ImageView(holder.itemView.context).apply {
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
            }
        }

        // 2. Animations (Fade in/out)
        holder.imageSwitcher.setInAnimation(holder.itemView.context, android.R.anim.fade_in)
        holder.imageSwitcher.setOutAnimation(holder.itemView.context, android.R.anim.fade_out)

        // 3. URLs
        val remoteImages = exercise.images?.map {
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/$it"
        } ?: emptyList()

        if (remoteImages.isNotEmpty()) {
            // first image load
            Glide.with(holder.itemView.context)
                .load(remoteImages[0])
                .into(holder.imageSwitcher.nextView as ImageView)
            holder.imageSwitcher.showNext()

            // 4. Auto-Switch Logic: Jo 2nd image hoy to loop chalu karo
            if (remoteImages.size > 1) {
                val handler = Handler(Looper.getMainLooper())
                var currentIndex = 0

                val runnable = object : Runnable {
                    override fun run() {

                        val context = holder.itemView.context
                        if(context is android.app.Activity && (context.isFinishing || context.isDestroyed)){
                            return // if activity goes to shutdown than don't go furthure
                        }
                        currentIndex = if (currentIndex == 0) 1 else 0
                        val nextView = holder.imageSwitcher.nextView as ImageView

                        if(nextView!=null){
                            Glide.with(holder.itemView.context)
                                .load(remoteImages[currentIndex])
                                .into(nextView)

                            holder.imageSwitcher.showNext()
                            handler.postDelayed(this, 1500) // 1.5 second ma switch thase
                        }

                    }
                }
                // Stop any previous animations for this view to avoid flickering
                holder.imageHandler = handler
                holder.imageRunnable = runnable
                handler.postDelayed(runnable, 1500)
            }
        }

        // --- CHECKBOX LOGIC ---
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = exercise.isSelected
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            exercise.isSelected = isChecked
            exerciseList.find { it.name == exercise.name }?.isSelected = isChecked
            onSelectionChanged(exerciseList.filter { it.isSelected }.size)
        }
    }

    override fun getItemCount() = filteredList.size

    fun updateList(newList: List<ExerciseModel>) {
        filteredList.clear()
        filteredList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getSelectedExercises(): List<ExerciseModel> {
        return exerciseList.filter { it.isSelected }
    }
}
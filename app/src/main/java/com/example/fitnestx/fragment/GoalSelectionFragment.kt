package com.example.fitnestx.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.fitnestx.R
import com.example.fitnestx.userMainActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GoalSelectionFragment : Fragment() {

    private var selectedGoal: String = "Weight Loss"
    private var selectedLevel : String = "Beginner"
    private lateinit var etGoalWeight : TextInputEditText
    private var goalWeight: Double? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Goal Views
        val btnWeightLoss = view.findViewById<TextView>(R.id.btnWeightLoss)
        val btnMuscleGain = view.findViewById<TextView>(R.id.btnMuscleGain)
        etGoalWeight = view.findViewById<TextInputEditText>(R.id.etGoalWeight)


        //Level Views
        val btnBeginner = view.findViewById<TextView>(R.id.btnBeginner)
        val btnIntermediate = view.findViewById<TextView>(R.id.btnIntermediate)
        val btnPro = view.findViewById<TextView>(R.id.btnPro)

        val btnFinish = view.findViewById<Button>(R.id.btnFinish)

        //goal click logic
        btnWeightLoss.setOnClickListener {
            selectedGoal = "Weight Loss"
            updateGoalUI(btnWeightLoss , btnMuscleGain)
        }
        btnMuscleGain.setOnClickListener {
            selectedGoal = "Muscle Gain"
            updateGoalUI(btnMuscleGain , btnWeightLoss)
        }

        //level click logic
        btnBeginner.setOnClickListener {
            selectedLevel = "Beginner"
            updatedLevelUI(btnBeginner , btnIntermediate , btnPro)

        }

        btnIntermediate.setOnClickListener {
            selectedLevel = "Intermediate"
            updatedLevelUI(btnIntermediate , btnBeginner , btnPro)
        }

        btnPro.setOnClickListener {
            selectedLevel = "Pro"
            updatedLevelUI(btnPro , btnBeginner , btnIntermediate)
        }

        btnFinish.setOnClickListener {
            saveToDataBase()
        }
    }

    private fun updateGoalUI(selected : TextView , unselected : TextView){
        selected.setBackgroundResource(R.drawable.bg_selection_selected)
        selected.setTextColor(resources.getColor(R.color.white))

        unselected.setBackgroundResource(R.drawable.bg_selection_unselected)
        unselected.setTextColor(resources.getColor(R.color.black))
    }

    private fun updatedLevelUI(selected : TextView , un1 : TextView , un2 : TextView){
        selected.setBackgroundResource(R.drawable.bg_selection_selected)
        selected.setTextColor(resources.getColor(R.color.white))

        un1.setBackgroundResource(R.drawable.bg_selection_unselected)
        un1.setTextColor(resources.getColor(R.color.black))

        un2.setBackgroundResource(R.drawable.bg_selection_unselected)
        un2.setTextColor(resources.getColor(R.color.black))
    }

    private fun saveToDataBase(){

        val goalWeightText = etGoalWeight.text.toString()
        goalWeight = goalWeightText.toDoubleOrNull()

        if(goalWeight == null){
            Toast.makeText(requireContext(),
                "Enter goal weight!",
                Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()
            .getReference("AppUsers")
            .child(uid)

        // fetch current weight from database
        database.get().addOnSuccessListener { snapshot ->

            val currentWeight =
                snapshot.child("weight")
                    .getValue(Double::class.java) ?: 0.0

            val updates = mapOf(

                "goalType" to selectedGoal,
                "fitnessLevel" to selectedLevel,

                // 🔥 NEW IMPORTANT FIELDS
                "startWeight" to currentWeight,
                "currentWeight" to currentWeight,
                "goalWeight" to goalWeight,

                "isProfileComplete" to true
            )

            database.updateChildren(updates)
                .addOnSuccessListener {

                    Toast.makeText(requireContext(),
                        "Profile Completed!",
                        Toast.LENGTH_SHORT).show()

                    startActivity(
                        Intent(requireContext(),
                            userMainActivity::class.java)
                    )
                    activity?.finish()
                }
        }
    }

}
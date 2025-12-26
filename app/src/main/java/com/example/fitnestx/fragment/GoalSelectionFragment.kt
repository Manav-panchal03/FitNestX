package com.example.fitnestx.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.fitnestx.R
import com.example.fitnestx.userMainActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GoalSelectionFragment : Fragment() {

    private var selectedGoal: String = "Weight Loss"
    private var selectedLevel: String = "Beginner"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnWeightLoss = view.findViewById<Button>(R.id.btnWeightLoss)
        val btnMuscleGain = view.findViewById<Button>(R.id.btnMuscleGain)
        val chipGroupLevel = view.findViewById<ChipGroup>(R.id.chipGroupLevel)
        val btnFinish = view.findViewById<Button>(R.id.btnFinish)


        btnWeightLoss.setOnClickListener {
            selectedGoal = "Weight Loss"
        }
        btnMuscleGain.setOnClickListener {
            selectedGoal = "Muscle Gain"
        }

        chipGroupLevel.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(checkedIds[0])
            selectedLevel = chip?.text?.toString() ?: "Beginner"
        }

        btnFinish.setOnClickListener {
            saveToDataBase()
        }
    }

    private fun saveToDataBase(){
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("AppUsers").child(uid)

        val data = mapOf(
            "goalType" to selectedGoal,
            "fitnessLevel" to selectedLevel ,
            "isProfileComplete" to true
        )

        database.updateChildren(data).addOnSuccessListener {

            Toast.makeText(requireContext(), "Profile Setup Complete !", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext() , userMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }
}
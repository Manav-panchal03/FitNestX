package com.example.fitnestx

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton

class WorkoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnCreateRoutine = view.findViewById<MaterialButton>(R.id.btnCreateRoutine)

        btnCreateRoutine.setOnClickListener {
            val intent = Intent(requireContext() , CreateWorkOutActivity::class.java)
            startActivity(intent)
        }
    }
}
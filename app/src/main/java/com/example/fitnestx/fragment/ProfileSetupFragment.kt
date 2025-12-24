package com.example.fitnestx.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.fitnestx.R
import com.example.fitnestx.userMainActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileSetupFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var selectedGender: String = "Male"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("AppUsers")

        val etAge = view.findViewById<TextInputEditText>(R.id.etAge)
        val etHeight = view.findViewById<TextInputEditText>(R.id.etHeight)
        val etWeight = view.findViewById<TextInputEditText>(R.id.etWeight)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val progressBar = view.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.setupProgress)
        val btnMale = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnMale)
        val btnFemale = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFemale)

        btnMale.setOnClickListener {
            selectedGender  = "Male"
            btnMale.setBackgroundColor(resources.getColor(R.color.brand_red))
            btnFemale.setBackgroundColor(resources.getColor(R.color.text_gray))
        }

        btnFemale.setOnClickListener {
            selectedGender = "Female"
            btnFemale.setBackgroundColor(resources.getColor(R.color.brand_red))
            btnMale.setBackgroundColor(resources.getColor(R.color.text_gray))
        }

        btnNext.setOnClickListener {
            val age = etAge.text.toString().toIntOrNull()
            val height = etHeight.text.toString().toDoubleOrNull()
            val weight = etWeight.text.toString().toDoubleOrNull()

            if(age == null || height == null || weight == null){
                Toast.makeText(requireContext(), "Fill All Details !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.setProgress(100 , true)
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            val updatedUser = mapOf(
                "age" to age,
                "gender" to selectedGender,
                "height" to height,
                "weight" to weight,
                "isProfileComplete" to true
            )

            database.child(uid).updateChildren(updatedUser).addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Ready !", Toast.LENGTH_SHORT).show()

                startActivity(Intent(requireContext() , userMainActivity::class.java))
                activity?.finish()
            }.addOnFailureListener {
                progressBar.setProgress(50 , true)
                Toast.makeText(requireContext(), "Firebase Error !", Toast.LENGTH_SHORT).show()
            }

        }

   }
}
package com.example.fitnestx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.pow

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return
        dbRef = FirebaseDatabase.getInstance().getReference("AppUsers").child(uid)

        // Bind Views
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val btnLogout = view.findViewById<View>(R.id.btnLogout)

        // Bind Stat Cards (using the IDs from the <include> tags)
        val viewHeight = view.findViewById<View>(R.id.cardHeight)
        val viewWeight = view.findViewById<View>(R.id.cardWeight)
        val viewAge = view.findViewById<View>(R.id.cardAge)

        fetchUserData(tvName, viewHeight, viewWeight, viewAge)

        btnLogout.setOnClickListener {
            auth.signOut()
            activity?.finish() // Closes MainActivity and returns to Login
        }
    }

    private fun fetchUserData(nameTv: TextView, hCard: View, wCard: View, aCard: View) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(AppUsers::class.java) ?: return

                // 1. Set Name
                nameTv.text = user.name

                // 2. Set Stat Card Values
                hCard.findViewById<TextView>(R.id.tvStatValue).text = "${user.height}cm"
                hCard.findViewById<TextView>(R.id.tvStatLabel).text = "Height"

                wCard.findViewById<TextView>(R.id.tvStatValue).text = "${user.weight}kg"
                wCard.findViewById<TextView>(R.id.tvStatLabel).text = "Weight"

                aCard.findViewById<TextView>(R.id.tvStatValue).text = "${user.age}yo"
                aCard.findViewById<TextView>(R.id.tvStatLabel).text = "Age"

                // 3. Calculate and Update BMI
                // Convert whatever the type is to Double safely
                val height = user.height.toString().toDoubleOrNull() ?: 0.0
                val weight = user.weight.toString().toDoubleOrNull() ?: 0.0

                calculateBMI(height, weight)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateBMI(heightCm: Double, weightKg: Double) {
        if (heightCm > 0 && weightKg > 0) {
            // Formula: weight (kg) / [height (m)]^2
            val heightM = heightCm / 100
            val bmi = weightKg / heightM.pow(2)

            val status = when {
                bmi < 18.5 -> "Underweight"
                bmi < 25.0 -> "Normal weight"
                bmi < 30.0 -> "Overweight"
                else -> "Obese"
            }

            // Update the BMI Banner
            view?.findViewById<TextView>(R.id.tvBMIStatus)?.text =
                "Your BMI is ${String.format("%.1f", bmi)} ($status)"
        }
    }
}
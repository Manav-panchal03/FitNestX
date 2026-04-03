package com.example.fitnestx

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
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
        val UserProgramTv = view.findViewById<TextView>(R.id.UserProgramTv)
        val btnLogout = view.findViewById<View>(R.id.btnLogout)

        // Bind Stat Cards (using the IDs from the <include> tags)
        val viewHeight = view.findViewById<View>(R.id.cardHeight)
        val viewWeight = view.findViewById<View>(R.id.cardWeight)
        val viewAge = view.findViewById<View>(R.id.cardAge)
        val btnViewDetails = view.findViewById<View>(R.id.btnViewDetails)
        val btnAnalytics = view.findViewById<View>(R.id.btnAnalytics)


        btnViewDetails.setOnClickListener {
            startActivity(
                Intent(requireContext(), BMIInfoActivity::class.java)
            )
        }

        btnAnalytics.setOnClickListener {
            startActivity(
                Intent(requireContext(), AnalyticsActivity::class.java)
            )

        }

        fetchUserData(tvName, UserProgramTv,viewHeight, viewWeight, viewAge)

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sign out")
                .setMessage("Do you really want to sign out?")
                .setPositiveButton("Sign out") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    activity?.finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
           // auth.signOut()
            //activity?.finish() // Closes MainActivit    y and returns to Login
        }

        val btnEditProfile = view.findViewById<View>(R.id.ivEditProfilePic)

        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

    }

    private fun fetchUserData(nameTv: TextView,UserProgramTv : TextView ,hCard: View, wCard: View, aCard: View) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(AppUsers::class.java) ?: return

                // 1. Set Name
                nameTv.text = user.name
                UserProgramTv.text = user.goalType


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

    private fun showEditProfileDialog() {

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profile, null)

        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etEditName)
        val etBio = dialogView.findViewById<android.widget.EditText>(R.id.etEditBio)
        val etGoalWeight = dialogView.findViewById<android.widget.EditText>(R.id.etGoalWeight)
        val spGoalType = dialogView.findViewById<android.widget.Spinner>(R.id.spGoalType)
        val btnSave = dialogView.findViewById<View>(R.id.btnSaveProfile)

        val passwordSection = dialogView.findViewById<View>(R.id.layoutPasswordSection)
        val btnChangePassword = dialogView.findViewById<View>(R.id.btnChangePassword)

        // ✅ Spinner data
        val goals = arrayOf("Weight Loss", "Muscle Gain", "Maintain")
        spGoalType.adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            goals
        )

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // 🔥 Prefill existing data
        dbRef.get().addOnSuccessListener { snap ->
            val user = snap.getValue(AppUsers::class.java) ?: return@addOnSuccessListener

            etName.setText(user.name)
            etBio.setText(user.bio)
            etGoalWeight.setText(user.goalWeight?.toString() ?: "")

            val index = goals.indexOf(user.goalType ?: "")
            if (index >= 0) spGoalType.setSelection(index)
        }

        // ✅ SAVE
        btnSave.setOnClickListener {

            val oldPass = dialogView.findViewById<EditText>(R.id.etOldPassword).text.toString()
            val newPass = dialogView.findViewById<EditText>(R.id.etNewPassword).text.toString()
            val confirmPass = dialogView.findViewById<EditText>(R.id.etConfirmPassword).text.toString()

// Only change password if user entered something
            if (oldPass.isNotEmpty() || newPass.isNotEmpty() || confirmPass.isNotEmpty()) {

                if (newPass != confirmPass) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val user = auth.currentUser!!
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(newPass)
                    }
            }

            val updates = hashMapOf<String, Any?>()

            updates["name"] = etName.text.toString().trim()
            updates["bio"] = etBio.text.toString().trim()
            updates["goalWeight"] = etGoalWeight.text.toString().toDoubleOrNull()
            updates["goalType"] = spGoalType.selectedItem.toString()

            dbRef.updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                }
        }

        btnChangePassword.setOnClickListener {

            if (passwordSection.visibility == View.GONE) {
                passwordSection.visibility = View.VISIBLE
                btnChangePassword.animate().rotation(180f).setDuration(200).start()
            } else {
                passwordSection.visibility = View.GONE
                btnChangePassword.animate().rotation(0f).setDuration(200).start()
            }
        }

        dialog.show()
    }

}
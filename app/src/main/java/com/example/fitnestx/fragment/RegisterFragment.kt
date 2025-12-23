package com.example.fitnestx.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.fitnestx.AppUsers
import com.example.fitnestx.R
import com.example.fitnestx.userMainActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class RegisterFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var usersRef : DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_register, container, false)

        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)


        val tilFullName = view.findViewById<TextInputLayout>(R.id.tilFullName)
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val tilConfirmPassword = view.findViewById<TextInputLayout>(R.id.tilConfirmPassword)


        val btnSignUp = view.findViewById<Button>(R.id.btnSignUp)
        val btnGoToLogin = view.findViewById<Button>(R.id.btnGoToLogin)

        auth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().getReference("AppUsers")

        // for now just basic valiodation + toast ( no firbase yet )
        btnSignUp.setOnClickListener {
            val name = etFullName.text?.toString()?.trim().orEmpty()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString()?.trim().orEmpty()
            val confirmPassword = etConfirmPassword.text?.toString()?.trim().orEmpty()

            // validation
            if (name.isEmpty()) {
                tilFullName.error = "Name required"
                return@setOnClickListener
            } else tilFullName.error = null

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Invalid email"
                return@setOnClickListener
            } else tilEmail.error = null

            if (password.length < 6) {
                tilPassword.error = "Min 6 characters"
                return@setOnClickListener
            } else tilPassword.error = null

            if (password != confirmPassword) {
                tilConfirmPassword.error = "Passwords don’t match"
                return@setOnClickListener
            } else tilConfirmPassword.error = null

            btnSignUp.isEnabled = false

            auth.createUserWithEmailAndPassword(email , password)
                .addOnCompleteListener { task ->
                    btnSignUp.isEnabled = true
                    if(task.isSuccessful){
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        SaveUserToDataBase(uid = uid , name = name ,email = email)
                    }
                    else{
                        Log.d(it.toString(), "Error : ${task.exception?.localizedMessage}")
                        Toast.makeText(requireContext(), "Registration Failed ! ", Toast.LENGTH_SHORT).show()
                    }
                }

            etEmail.clearFocus()
            etPassword.clearFocus()
            etConfirmPassword.clearFocus()
            etFullName.clearFocus()
        }

        //back to login
        btnGoToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun SaveUserToDataBase(uid : String , name : String , email : String) {
        val user = AppUsers(uid = uid, name = name, email = email)
        usersRef.child(uid).setValue(user)
            .addOnSuccessListener {
                showSuccessDialog()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to Create Account ! ", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSuccessDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_register_success, null , false)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            dialog.dismiss()
            // After user taps OK, go back to LoginFragment
            parentFragmentManager.popBackStack()
        }
        dialog.show()
    }
}
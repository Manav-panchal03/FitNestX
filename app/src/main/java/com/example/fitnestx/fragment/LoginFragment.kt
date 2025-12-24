package com.example.fitnestx.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.fitnestx.R
import com.example.fitnestx.userMainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class LoginFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_login, container, false)

        val emailEditText = view.findViewById<EditText>(R.id.etEmail)
        val passwordEditText = view.findViewById<EditText>(R.id.etPassword)

        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)

        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val BtnGoToRegister = view.findViewById<TextView>(R.id.btnGoToRegister)
        val btnGoogleLogin = view.findViewById<MaterialButton>(R.id.btnGoogleLogin)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("AppUsers")
        // For now, Login button does nothing (we'll add Firebase later)
        loginButton.setOnClickListener {
            // Later: validate and call Firebase signInWithEmailAndPassword
            val email = emailEditText.text?.toString()?.trim().orEmpty()
            val password = passwordEditText.text?.toString()?.trim().orEmpty()

//            if(email.isEmpty() || password.isEmpty()){
//                Toast.makeText(requireContext(), "Enter Email and Password ! ", Toast.LENGTH_SHORT).show()
//            }
//            else{
//                loginButton.isEnabled = false
//                auth.signInWithEmailAndPassword(email , password)
//                    .addOnCompleteListener { task ->
//                        loginButton.isEnabled = true
//                        if(task.isSuccessful){
//                            Toast.makeText(requireContext(), "Logged In !" , Toast.LENGTH_SHORT).show()
//                            startActivity(Intent(requireContext(), userMainActivity::class.java))
//                            requireActivity().finish()
//                        }
//                        else{
//                            Toast.makeText(requireContext(), "Login failed : ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//            }


            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Invalid email"
                return@setOnClickListener
            } else tilEmail.error = null

            if (password.isEmpty()) {
                tilPassword.error = "Password required"
                return@setOnClickListener
            } else tilPassword.error = null

            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(email , password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val user = auth.currentUser

                        user?.reload()?.addOnCompleteListener {
                            if(user != null && user.isEmailVerified){
                                checkProfileStatus(user.uid) // if email verify , than check profile status
                            }
                            else{
                                loginButton.isEnabled = true
                                auth.signOut()
                                Toast.makeText(requireContext(), "Please Verify email before login ! ", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else{
                        loginButton.isEnabled = true
                        Toast.makeText(requireContext(), "Login Failed ! ", Toast.LENGTH_SHORT).show()
                    }
                }

        }

        // Go to RegisterFragment when text is clicked
        BtnGoToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        btnGoogleLogin.setOnClickListener {
            Toast.makeText(requireContext(), "Google Login Coming Soon !", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun checkProfileStatus(uid : String){
        // ERROR: Jya sudhi upar ni 'database =' wali line execute nahi thay, tya sudhi aa crash thase.
        // Safe check mate tame aa line add kari sako:
        if (!::database.isInitialized) {
            database = FirebaseDatabase.getInstance().getReference("AppUsers")
        }
        database.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()){
                val isComplete = snapshot.child("isProfileComplete").getValue(Boolean::class.java) ?: false

                if(isComplete){
                    val intent = Intent(requireContext(), userMainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                else{
                    navigateToProfileSetUp() //if profile imncomplete
                }
            }
            else{
                navigateToProfileSetUp() // first time login , than also go to profile setup
            }
        }.addOnFailureListener {
            view?.findViewById<Button>(R.id.btnLogin)?.isEnabled = true
            Toast.makeText(requireContext(), "DataBase error : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToProfileSetUp(){
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileSetupFragment())
            .commit()
    }
}
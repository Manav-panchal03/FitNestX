package com.example.fitnestx

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import nl.joery.animatedbottombar.AnimatedBottomBar

class userMainActivity : AppCompatActivity() {

//    private lateinit var auth : FirebaseAuth
//    private lateinit var btnLogout : MaterialButton
    private lateinit var bottombar : AnimatedBottomBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        auth = FirebaseAuth.getInstance()

//        btnLogout = findViewById(R.id.btnLogout)
//        btnLogout.setOnClickListener {
//            // 1) Firebase sign out (works for email + Google providers)
//            auth.signOut()
//            // 2) Go back to AuthActivity and clear back stack
//            val intent = Intent(this, AuthActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//        }
        bottombar = findViewById(R.id.bottomBar)
        loadFragment(HomeFragment())

        bottombar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                when (newTab.id) {
                    R.id.nav_home -> loadFragment(HomeFragment())
                    R.id.nav_workouts -> loadFragment(WorkoutFragment())
                    R.id.nav_habits -> loadFragment(HabitsFragment())
                    R.id.nav_profile -> loadFragment(ProfileFragment())
                }
            }

        })


    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.containerUser, fragment)
            .commit()
    }
}
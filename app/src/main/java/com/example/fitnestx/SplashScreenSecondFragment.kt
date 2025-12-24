package com.example.fitnestx

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.fitnestx.fragment.LoginFragment
import com.example.fitnestx.fragment.ProfileSetupFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenSecondFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_splash_screen_second, container, false)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvSubText = view.findViewById<TextView>(R.id.tvSubText)

        val maintext = "Welcome to FitNestX"
        val subtext = "Let's Start Your Journey..."

        lifecycleScope.launch {
            //1st line
            tvWelcome.text = ""
            for(letter in maintext){
                tvWelcome.append(letter.toString())
                delay(80)
            }
            delay(300)

            //2nd line
            tvSubText.text = ""
            tvSubText.visibility = View.VISIBLE

            for(letter in subtext){
                tvSubText.append(letter.toString())
                delay(50)
            }
            delay(1500)
            navigationNext()
        }

        return view
    }

    private fun navigationNext(){
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in , android.R.anim.fade_out)
            .replace(R.id.fragment_container, ProfileSetupFragment())
            .commit()
    }

}
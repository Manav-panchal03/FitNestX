package com.example.fitnestx

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Lottie એનિમેશન પૂરું થાય એટલે સેશન ચેક કરો
        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                // જો લોગિન હોય તો -> સીધા userMainActivity (Home) પર જાઓ
                val intent = Intent(this, userMainActivity::class.java)
                startActivity(intent)
            } else {
                // જો લોગિન ના હોય તો -> AuthActivity (Login) પર જાઓ
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }
            finish() // Splash બંધ કરો
        }, 2000) // 3 સેકન્ડનો ટાઈમ
    }
}
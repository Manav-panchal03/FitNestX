package com.example.fitnestx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class BMIInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmiinfo)

        val tvInfo = findViewById<TextView>(R.id.tvBmiInfo)

        tvInfo.text = """
            BMI Categories:
            
            Underweight: < 18.5
            Normal Weight: 18.5 – 24.9
            Overweight: 25 – 29.9
            Obese: 30+
            
            BMI (Body Mass Index) measures body fat
            based on height and weight.
            
            It helps track fitness progress.
        """.trimIndent()
    }
}

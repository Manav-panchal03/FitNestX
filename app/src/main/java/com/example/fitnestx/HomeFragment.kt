package com.example.fitnestx

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text


class HomeFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var dbRef : DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return
        dbRef = FirebaseDatabase.getInstance().getReference("AppUsers").child(uid)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvBmiValue = view.findViewById<TextView>(R.id.tvBmiValue)
        val tvBmiCategory = view.findViewById<TextView>(R.id.tvBmiCategory)
        val tvUserGoal = view.findViewById<TextView>(R.id.tvUserGoal)

        //Fetching data from firebase
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val name = snapshot.child("name").value.toString()
                    val weight = snapshot.child("weight").value.toString().toDoubleOrNull() ?: 0.0
                    val height = snapshot.child("height").value.toString().toDoubleOrNull() ?: 0.0
                    val goal = snapshot.child("goalType").value.toString()

                    tvWelcome.text = "Hello, $name!"
                    tvUserGoal.text = goal

                    //bmi colculate
                    if(height > 0){
                        val heightInmeter = height / 100
                        val bmi = weight / (heightInmeter * heightInmeter)
                        tvBmiValue.text = String.format("%.2f", bmi)
                        tvBmiCategory.text = getBmiCategory(bmi)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getBmiCategory(bmi : Double) : String {
        return when{
            bmi < 18.5 -> "Underweight"
            bmi in 18.5..24.9 -> "Normal Weight"
            bmi in 25.0..29.9 -> "Overweight"
            else -> "Obese"
        }

    }
}
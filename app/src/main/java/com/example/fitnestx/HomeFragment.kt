package com.example.fitnestx

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var dbRef : DatabaseReference
    private lateinit var dbWater : DatabaseReference
    private lateinit var dbSleep : DatabaseReference
    private lateinit var todayDate : String
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

        //today's date setup
        todayDate = SimpleDateFormat("yyyy-MM-dd" , Locale.getDefault()).format(Date())
        dbRef = FirebaseDatabase.getInstance().getReference("AppUsers").child(uid)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvBmiValue = view.findViewById<TextView>(R.id.tvBmiValue)
        val tvBmiCategory = view.findViewById<TextView>(R.id.tvBmiCategory)
        val ivBmiStatus = view.findViewById<ImageView>(R.id.ivBmiStatus)
        val tvUserGoal = view.findViewById<TextView>(R.id.tvUserGoal)
        val waterBar = view.findViewById<ProgressBar>(R.id.waterProgressBar)
        val tvWater = view.findViewById<TextView>(R.id.tvWaterStats)
        val tvSleep = view.findViewById<TextView>(R.id.tvSleepStats)
        val btnLogWater = view.findViewById<Button>(R.id.btnLogWater)
        val btnLogSleep = view.findViewById<Button>(R.id.btnLogSleep)


        //Fetching data from firebase
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
//                    val name = snapshot.child("name").value.toString()
//                    val weight = snapshot.child("weight").value.toString().toDoubleOrNull() ?: 0.0
//                    val height = snapshot.child("height").value.toString().toDoubleOrNull() ?: 0.0
//                    val goal = snapshot.child("goalType").value.toString()
//
//                    tvWelcome.text = "Hello, $name!"
//                    tvUserGoal.text = goal
//
//                    //bmi colculate
//                    if(height > 0){
//                        val heightInmeter = height / 100
//                        val bmi = weight / (heightInmeter * heightInmeter)
//                        tvBmiValue.text = String.format("%.2f", bmi)
//                        tvBmiCategory.text = getBmiCategory(bmi)
//                    }

                    val user = snapshot.getValue(AppUsers::class.java) ?: return

                    tvWelcome.text = "Hello , ${user.name}!"
                    tvUserGoal.text = user.goalType ?: "No Goal Set"

                    //water & sleeep status
                    tvSleep.text = "${user.sleepHours} / 7 hrs"
                    val waterGoal = 3.0// standard goal
                    val waterPercentage = (user.waterIntake / waterGoal) * 100
                    waterBar.progress = waterPercentage.toInt()
                    tvWater.text = "${user.waterIntake} / ${waterGoal} L"

                    //bmi calculation
                    if(user.height != null && user.weight != null && user.height > 0){
                        val heightInMeter = user.height / 100
                        val bmi = user.weight / (heightInMeter * heightInMeter)
                        tvBmiValue.text = String.format("%.2f", bmi)

                        val category = getBmiCategory(bmi)
                        tvBmiCategory.text = category
                        updateBmiImage(category , ivBmiStatus)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        btnLogWater.setOnClickListener {
            showLogDialog("Water" , "waterIntake" , arrayOf("Add 250ml" , "Add 500ml" , "Custom Amount" , "Reset"))
        }

        btnLogSleep.setOnClickListener {
            showLogDialog("Sleep" , "sleepHours" , arrayOf("Add 1 Hour" , "Add 2 Hours" , "Custom Amount" , "Reset"))
        }
    }

    private fun getBmiCategory(bmi : Double) : String {
        return when{
            bmi < 18.5 -> "Underweight"
            bmi in 18.5..24.9 -> "Normal Weight"
            bmi in 25.0..29.9 -> "Overweight"
            else -> "Obese"
        }

    }
    private fun updateBmiImage(category: String, imageView: ImageView) {
        when (category) {
            "Underweight" -> imageView.setImageResource(R.drawable.underweight)
            "Normal Weight" -> imageView.setImageResource(R.drawable.normalweight)
            "Overweight" -> imageView.setImageResource(R.drawable.overweight)
            "Obese" -> imageView.setImageResource(R.drawable.obese)
        }
    }

    private fun showLogDialog(title : String ,fields : String , options : Array<String>){
        AlertDialog.Builder(requireContext())
            .setTitle("Log $title")
            .setItems(options){ _, which ->
                when {
                    options[which] == "Custom Amount" -> showCustomInputDialog(title,fields)
                    options[which] == "Reset" -> dbRef.child(fields).setValue(0.0)
                    else -> {
                        val amount = if(fields == "waterIntake"){
                            if(which == 0) 0.25 else 0.50
                        } else {
                            if(which == 0) 1.0 else 2.0
                        }
                        updateValueInFirebase(fields , amount)
                    }

                }
            }.show()
    }

    private fun showCustomInputDialog(title: String , fields: String){
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.hint = if(fields == "waterIntake") "e.g. 0.75 (Liters)" else "e.g. 7.5 (Hours)"

        AlertDialog.Builder(requireContext())
            .setTitle("Enter Custom $title")
            .setView(editText)
            .setPositiveButton("Add"){ _, _ ->
                val input = editText.text.toString().toDoubleOrNull()
                if(input != null && input > 0){
                    updateValueInFirebase(fields , input)
                }
            }
            .setNegativeButton("Cancel" , null)
            .show()
    }

    private fun updateValueInFirebase(fields: String, amount: Double){
        dbRef.child(fields).runTransaction(object : Transaction.Handler{
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentVal = mutableData.getValue(Double::class.java) ?: 0.0
                mutableData.value = currentVal + amount
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if(committed) Toast.makeText(requireContext(), "Updated ! ", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
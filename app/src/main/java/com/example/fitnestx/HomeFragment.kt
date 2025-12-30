package com.example.fitnestx

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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


class HomeFragment : Fragment() , SensorEventListener {

    private lateinit var auth : FirebaseAuth
    private lateinit var dbRef : DatabaseReference
    private lateinit var dbWater : DatabaseReference
    private lateinit var dbSleep : DatabaseReference
    private lateinit var dbSteps : DatabaseReference
    private lateinit var todayDate : String


    //sensor variables
    private var sensorManager : SensorManager? = null
    private var stepSensor : Sensor? = null
    private lateinit var tvSteps : TextView

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

        //database references
        dbRef = FirebaseDatabase.getInstance().getReference("AppUsers").child(uid)
        dbWater = FirebaseDatabase.getInstance().getReference("WaterIntake").child(uid).child(todayDate)
        dbSleep = FirebaseDatabase.getInstance().getReference("SleepLog").child(uid).child(todayDate)
        dbSteps = FirebaseDatabase.getInstance().getReference("Steps").child(uid).child(todayDate).child("steps")

        //views")
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
        tvSteps = view.findViewById<TextView>(R.id.tvSteps)


        //sensor init
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        checkStepPermission()

        //Fetching data from firebase
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(AppUsers::class.java) ?: return
                    tvWelcome.text = "Hello , ${user.name}!"
                    tvUserGoal.text = user.goalType ?: "No Goal Set"
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

        dbWater.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val amount = snapshot.getValue(Double::class.java) ?: 0.0
                val waterPercent = (amount / 3.0) * 100
                waterBar.progress = waterPercent.toInt()
                tvWater.text = "$amount / 3.0 L"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        dbSleep.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val hours = snapshot.getValue(Double::class.java) ?: 0.0
                tvSleep.text = "$hours / 7 hrs"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        dbSteps.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val steps = snapshot.getValue(Int::class.java) ?: 0
                tvSteps.text = "$steps Steps"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        btnLogWater.setOnClickListener {
            showLogDialog("Water" , dbWater, arrayOf("Add 250ml" , "Add 500ml" , "Custom Amount" , "Reset"))
        }

        btnLogSleep.setOnClickListener {
            showLogDialog("Sleep" , dbSleep , arrayOf("Add 1 Hour" , "Add 2 Hours" , "Custom Amount" , "Reset"))
        }
    }

    //sensor logic
    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_STEP_COUNTER){
            updateStepsInFirebase(1)
        }
    }

    private fun updateStepsInFirebase(increment: Int){
        dbSteps.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentSteps = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentSteps + increment
                return Transaction.success(currentData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {}
        })
    }

    private fun checkStepPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Toast.makeText(context, "Permission denied for steps", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        stepSensor.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
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

    private fun showLogDialog(title: String, targetRef: DatabaseReference, options: Array<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Log $title")
            .setItems(options) { _, which ->
                when {
                    options[which] == "Custom Amount" -> showCustomInputDialog(title, targetRef)
                    options[which] == "Reset" -> targetRef.setValue(0.0)
                    else -> {
                        // Water mate 0.25/0.50 ane Sleep mate 1.0/2.0
                        val amount = if (title == "Water") (if (which == 0) 0.25 else 0.50) else (if (which == 0) 1.0 else 2.0)
                        updateValueInFirebase(targetRef, amount)
                    }
                }
            }.show()
    }

    private fun showCustomInputDialog(title: String, targetRef: DatabaseReference) {
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.hint = if (title == "Water") "e.g. 0.75 (Liters)" else "e.g. 7.5 (Hours)"

        AlertDialog.Builder(requireContext())
            .setTitle("Enter Custom $title")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val input = editText.text.toString().toDoubleOrNull()
                if (input != null && input > 0) {
                    updateValueInFirebase(targetRef, input)
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun updateValueInFirebase(targetRef: DatabaseReference, amount: Double) {
        targetRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentVal = mutableData.getValue(Double::class.java) ?: 0.0
                mutableData.value = currentVal + amount
                return Transaction.success(mutableData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (committed) Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
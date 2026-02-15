package com.example.fitnestx

import android.Manifest
import android.content.Context
import android.content.Intent
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
import kotlin.jvm.java


class HomeFragment : Fragment() , SensorEventListener {

    private lateinit var auth : FirebaseAuth
    private lateinit var dbRef : DatabaseReference
    private lateinit var dbWater : DatabaseReference
    private lateinit var dbSleep : DatabaseReference
    private lateinit var dbSteps : DatabaseReference
    private lateinit var dbWeightHistory: DatabaseReference

    private lateinit var todayDate : String

    private var startingWeight: Double = 0.0
    private var goalWeight: Double = 0.0
    private var currentWeight = 0.0


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
        dbSteps = FirebaseDatabase.getInstance().getReference("Steps").child(uid).child(todayDate)
        dbWeightHistory = FirebaseDatabase.getInstance().getReference("WeightHistory").child(uid)
        //views")
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvBmiValue = view.findViewById<TextView>(R.id.tvBmiValue)
        val tvBmiCategory = view.findViewById<TextView>(R.id.tvBmiCategory)
        val tvDate = view.findViewById<TextView>(R.id.tvCurrentDate)
        val tvTime = view.findViewById<TextView>(R.id.tvCurrentTime)
        val tvUserGoal = view.findViewById<TextView>(R.id.tvUserGoal)
        val waterBar = view.findViewById<ProgressBar>(R.id.waterProgressBar)
        val tvWater = view.findViewById<TextView>(R.id.tvWaterStats)
        val tvSleep = view.findViewById<TextView>(R.id.tvSleepStats)
        val btnLogWater = view.findViewById<Button>(R.id.btnLogWater)
        val btnLogSleep = view.findViewById<Button>(R.id.btnLogSleep)
        tvSteps = view.findViewById<TextView>(R.id.tvSteps)
        val weightProgressBar = view.findViewById<ProgressBar>(R.id.weightProgressBar)
        val tvWeightProgress = view.findViewById<TextView>(R.id.tvWeightProgress)
        val tvMotivation = view.findViewById<TextView>(R.id.tvMotivation)
        val btnLogWeight = view.findViewById<Button>(R.id.btnLogWeight)
        val tvCurrentWeight = view.findViewById<TextView>(R.id.tvCurrentWeight)
        val cardWeight = view.findViewById<View>(R.id.cardWeightProgress)



        //sensor init
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        checkStepPermission()
        updateDateTime(tvDate, tvTime)


        //Fetching data from firebase
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(AppUsers::class.java) ?: return
                    tvWelcome.text = "${user.name}!"
                    tvUserGoal.text = user.goalType ?: "No Goal Set"
                    //bmi calculation
                    if(user.height != null && user.weight != null && user.height > 0){
                        val heightInMeter = user.height / 100
                        val bmi = user.weight / (heightInMeter * heightInMeter)
                        tvBmiValue.text = String.format("%.2f", bmi)
                        val category = getBmiCategory(bmi)
                        tvBmiCategory.text = category
                    }

                    currentWeight = user.weight ?: 0.0
                    goalWeight = user.goalWeight ?: 0.0
                    tvCurrentWeight.text = String.format("%.1f kg", currentWeight)


                    // Set starting weight only once
                    if (user.startingWeight == null || user.startingWeight == 0.0) {

                        startingWeight = currentWeight

                        dbRef.child("startingWeight").setValue(currentWeight)

                    } else {
                        startingWeight = user.startingWeight!!
                    }

//                    if (currentWeight > 0) {
//                        saveWeightHistory(currentWeight)
//                    }

                    val progress = calculateWeightProgress()
                    weightProgressBar.progress = progress
                    tvWeightProgress.text = "$progress%"

                    showMotivation(progress , tvMotivation)
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
                try {
                    val steps = snapshot.getValue(Int::class.java) ?: 0
                    tvSteps.text = "$steps Steps"
                }catch (e: Exception){
                    tvSteps.text = "0 Steps"
                    android.util.Log.e("FirebaseError", "Data format mismatch: ${e.message}")
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        btnLogWater.setOnClickListener {
            showLogDialog("Water" , dbWater, arrayOf("Add 250ml" , "Add 500ml" , "Custom Amount" , "Reset"))
        }

        btnLogSleep.setOnClickListener {
            showLogDialog("Sleep" , dbSleep , arrayOf("Add 1 Hour" , "Add 2 Hours" , "Custom Amount" , "Reset"))
        }
        btnLogWeight.setOnClickListener {
            showWeightDialog()
        }
        updateWeightButtonState(btnLogWeight)

        cardWeight.setOnClickListener {
            startActivity(
                Intent(requireContext(), WeightProgressGraphActivity::class.java)
            )
            cardWeight.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    cardWeight.scaleX = 1f
                    cardWeight.scaleY = 1f
                }
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
            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if(error != null) android.util.Log.e("Firebase " , error.message)
            }
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
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getBmiCategory(bmi : Double) : String {
        return when{
            bmi < 18.5 -> "Underweight"
            bmi in 18.5..24.9 -> "Normal Weight"
            bmi in 25.0..29.9 -> "Overweight"
            else -> "Obese"
        }

    }
    private fun updateDateTime(tvDate: TextView, tvTime: TextView) {
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val currentDate = dateFormat.format(Date())
        val currentTime = timeFormat.format(Date())

        tvDate.text = currentDate
        tvTime.text = currentTime
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

    private fun saveWeightHistory(weight: Double) {

        val todayKey = todayDate // yyyy-MM-dd

        val entry = WeightHistoryModel(
            weight = weight,
            timestamp = System.currentTimeMillis()
        )

        dbWeightHistory.child(todayKey).setValue(entry)
    }



    private fun calculateWeightProgress(): Int {

        if (goalWeight == 0.0 || startingWeight == goalWeight) return 0

        val progress = if (goalWeight > startingWeight) {
            // Muscle gain
            (currentWeight - startingWeight) /
                    (goalWeight - startingWeight)
        } else {
            // Fat loss
            (startingWeight - currentWeight) /
                    (startingWeight - goalWeight)
        }

        return (progress * 100)
            .coerceIn(0.0, 100.0)
            .toInt()
    }

    private fun showMotivation(progress: Int, tv: TextView) {

        val message = when {

            progress == 0 ->
                "Let's start your journey 🚀"

            progress in 1..25 ->
                "Good start! Keep pushing 💪"

            progress in 26..50 ->
                "Halfway energy building 🔥"

            progress in 51..75 ->
                "Strong progress! Don't stop 🏋️"

            progress in 76..99 ->
                "Almost there! Finish strong 👑"

            progress >= 100 ->
                "GOAL ACHIEVED! 🎉"

            else ->
                "Stay consistent 💯"
        }

        tv.text = message
    }

    private fun showWeightDialog() {

        dbWeightHistory.child(todayDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        updateWeightButtonState(
                            requireView().findViewById(R.id.btnLogWeight)
                        )
                        return
                    }


                    val edit = EditText(requireContext())
                    edit.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    edit.hint = "Enter weight (kg)"

                    AlertDialog.Builder(requireContext())
                        .setTitle("Log Weight")
                        .setView(edit)
                        .setPositiveButton("Save") { _, _ ->

                            val weight =
                                edit.text.toString().toDoubleOrNull()

                            if (weight != null && weight > 0) {

                                dbRef.child("weight").setValue(weight)
                                saveWeightHistory(weight)

                                Toast.makeText(
                                    context,
                                    "Weight updated!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                updateWeightButtonState(
                                    requireView().findViewById(R.id.btnLogWeight)
                                )

                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateWeightButtonState(btn: Button) {

        dbWeightHistory.child(todayDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        // Already logged today
                        btn.text = "Logged Today ✓"
                        btn.setTextColor(
                            ContextCompat.getColor(requireContext() , R.color.app_theme2)
                        )
                        btn.isEnabled = false
                        btn.setBackgroundColor(
                            ContextCompat.getColor(requireContext() , android.R.color.transparent)
                        )
                        btn.alpha = 0.6f

                    } else {

                        // Not logged
                        btn.text = "Log Weight"
                        btn.isEnabled = true
                        btn.alpha = 1f
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


}
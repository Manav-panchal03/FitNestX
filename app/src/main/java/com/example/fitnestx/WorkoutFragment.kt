package com.example.fitnestx

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WorkoutFragment : Fragment() {

    private lateinit var rvRoutines: RecyclerView
    private lateinit var routineAdapter: RoutineAdapter
    private var routineList = mutableListOf<RoutineModel>()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 1. view init
        val btnCreateRoutine = view.findViewById<MaterialButton>(R.id.btnCreateRoutine)
        rvRoutines = view.findViewById<RecyclerView>(R.id.rvMyRoutines)

        // 2. RecyclerView Setup
        rvRoutines.layoutManager = LinearLayoutManager(requireContext())
        routineAdapter = RoutineAdapter(routineList){selectedRoutine ->
            // user press start routine button
            Toast.makeText(requireContext(), "Starting: ${selectedRoutine.routineName}", Toast.LENGTH_SHORT).show()
            // -------------- ahiya new LogWorkoutActivity chalu thase ---------------------------- //
        }
        rvRoutines.adapter = routineAdapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if(userId != null){
            database = FirebaseDatabase.getInstance().getReference("Routines").child(userId)
           fetchRoutines()
        }

        btnCreateRoutine.setOnClickListener {
            val intent = Intent(requireContext() , CreateWorkOutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchRoutines(){
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                routineList.clear()
                for(postSnapshot in snapshot.children){
                    val routine = postSnapshot.getValue(RoutineModel::class.java)
                    if (routine != null) {
                        routineList.add(routine)
                    }
                }
                routineAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }
}

package com.example.softwaredevelopment23_24

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.softwaredevelopment23_24.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

const val thirstDivisor: Int = 2
const val enjoymentDivisor: Int = 3
const val hungerDivisor: Int = 5
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var reference: DatabaseReference
    private lateinit var user: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController((navController))

        user = FirebaseAuth.getInstance()

        if (user.currentUser != null) {
            navController.navigate(R.id.navigation_home)
            val userID = user.currentUser!!.uid
            reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)
            updateLoginTime(reference, this)
        } else {
            navController.navigate(R.id.navigation_login)
        }

    }

    fun generateDatabase(userID: String, username: String, email:String, context: Context) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        val currentTime = System.currentTimeMillis()
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "task1" to true,
            "task2" to true,
            "task3" to true,
            "task4" to true,
            "task5" to true,
            "task6" to false,
            "task7" to false,
            "task8" to false,
            "petHunger" to 50,
            "petThirst" to 50,
            "petEnjoyment" to 50,
            "coins" to 20,
            "loginTime" to currentTime,
            "timeDifference" to 0
        )
        database.child("users").child(userID).setValue(userData)
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Failed to add user to database. ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
    fun generateStats(
        reference: DatabaseReference, 
        context: Context,
        callback: (petHunger: Int, petThirst: Int, petEnjoyment: Int, userCoins: Int) -> Unit) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val petThirst = (snapshot.child("petThirst").getValue(Int::class.java) ?: 0)
                val petHunger = (snapshot.child("petHunger").getValue(Int::class.java) ?: 0)
                val petEnjoyment = (snapshot.child("petEnjoyment").getValue(Int::class.java) ?: 0)
                val coins = snapshot.child("coins").getValue(Int::class.java) ?: 0
                callback(petHunger, petThirst, petEnjoyment, coins)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not read data from database: ${error.toException()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        return
    }

    fun updateLoginTime(reference: DatabaseReference, context: Context) {
        val currentTime = System.currentTimeMillis()
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("loginTime")) {
                    val loginTime = snapshot.child("loginTime").getValue(Long::class.java)
                    val timeDifference = (currentTime - loginTime!!) / 60000
                    val hungerDepreciation = timeDifference / hungerDivisor
                    val thirstDepreciation = timeDifference / thirstDivisor
                    val enjoymentDepreciation = timeDifference / enjoymentDivisor

                    val petHunger = (snapshot.child("petHunger").value as Long - hungerDepreciation).toInt()
                    val petThirst = (snapshot.child("petThirst").value as Long - thirstDepreciation).toInt()
                    val petEnjoyment = (snapshot.child("petEnjoyment").value as Long - enjoymentDepreciation).toInt()

                    val timeData = hashMapOf(
                        "loginTime" to currentTime,
                        "petHunger" to maxOf(0, petHunger),
                        "petThirst" to maxOf(0, petThirst),
                        "petEnjoyment" to maxOf(0, petEnjoyment)
                    )

                    reference.updateChildren(timeData as Map<String, Any>)
                        .addOnCompleteListener{
                           if (!it.isSuccessful) {
                               Toast.makeText(
                                   context,
                                   "Failed to update login time",
                                   Toast.LENGTH_SHORT
                               ).show()
                           }
                        }
                } else {
                    Toast.makeText(
                        context,
                        "Login time not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Error reading login time ${error.toException()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
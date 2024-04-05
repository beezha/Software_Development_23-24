package com.example.softwaredevelopment23_24

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.softwaredevelopment23_24.databinding.FragmentPetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.properties.Delegates

// TODO: make UI updates cleaner when points are spent
// TODO: add in more error checking to stats that may become negative (this should never happen)
class Pet : Fragment() {
    private lateinit var binding: FragmentPetBinding
    private lateinit var user: FirebaseUser
    private var database =  FirebaseDatabase.getInstance()
    private lateinit var reference: DatabaseReference

    // TODO: add in stat degradation features
    // TODO: add in cleaner UI updates so it doesn't look so "stiff"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPetBinding.inflate(inflater, container, false)
        user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        reference = database.reference.child("users").child(userID)
        loadPetStats() // initial load of the stats + UI

        // checking each button for when they are clicked
        binding.hungerButton.setOnClickListener{
            spendPoints(it, reference)
        }
        binding.thirstButton.setOnClickListener{
            spendPoints(it, reference)
        }
        binding.funButton.setOnClickListener{
            spendPoints(it, reference)
        }
        return binding.root
    }

    //func for updating the UI to be in sync with the database
    private fun loadPetStats() {
        (activity as MainActivity).generateStats(reference, requireContext()) {petHunger, petThirst, petEnjoyment, coins ->
            binding.apply {
                hungerprogressBar.progress = petHunger
                thirstprogressBar.progress = petThirst
                funprogressBar.progress = petEnjoyment
                petcoinText.text = coins.toString()
            }
        }
    }

    // func for spending points
    private fun spendPoints(button: View, reference: DatabaseReference) {
        lateinit var newValues: HashMap<String, Any>
        var newStat by Delegates.notNull<Int>()
        var newCoins by Delegates.notNull<Int>()
        (activity as MainActivity).generateStats(reference, requireContext()) { petHunger, petThirst, petEnjoyment, coins ->
            // check to make sure that coin total is above 5
            if (coins >= 5) {
                // switch case to change what logic is being done for each possible button press
                when (button) {
                    binding.hungerButton -> {
                        newStat = petHunger + 15
                        newCoins = coins - 5
                        newValues = hashMapOf(
                            "petHunger" to newStat,
                            "coins" to newCoins
                        )
                    }
                    binding.thirstButton -> {
                        newStat = petThirst + 15
                        newCoins = coins - 5
                        newValues = hashMapOf(
                            "petThirst" to newStat,
                            "coins" to newCoins
                        )
                    }
                    else -> {
                        newStat = petEnjoyment + 15
                        newCoins = coins - 5
                        newValues = hashMapOf(
                            "petEnjoyment" to newStat,
                            "coins" to newCoins
                        )
                    }
                }
            reference.updateChildren(newValues) // updates the database with the new values
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // successful database update
                        loadPetStats() //loads UI to be in sync with database (or else it would happen to fast)
                    } else {
                        // unsuccessful database update
                        Toast.makeText(
                            requireContext(),
                            "Please Try Again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            //else case for when coin amount is too low
            } else {
                Toast.makeText(
                    requireContext(),
                    "Insufficient coins",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
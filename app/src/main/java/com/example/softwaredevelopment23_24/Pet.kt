package com.example.softwaredevelopment23_24

import android.content.Context
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.example.softwaredevelopment23_24.databinding.FragmentPetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.properties.Delegates

// TODO: make UI updates cleaner when points are spent
class Pet : Fragment() {
    private lateinit var binding: FragmentPetBinding
    private lateinit var user: FirebaseUser
    private var database =  FirebaseDatabase.getInstance()
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPetBinding.inflate(inflater, container, false)
        user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        reference = database.reference.child("users").child(userID)
        loadUI() // initial load of the UI

        // saves changes to pet name
        binding.btnPetSaveName.setOnClickListener{
            val newName = binding.etPetName.text.toString()
            updatePetName(newName)
        }
        binding.etPetName.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                val newName = binding.etPetName.text.toString()
                updatePetName(newName)
                true
            }
            else {
                false
            }
        }

        // start editing pet name
        binding.btnPetName.setOnClickListener {
            startEditing()
        }

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

    private fun loadUI() {
        loadPetName()
        loadPetStats()
    }

    private fun startEditing() {
        binding.btnPetName.isGone = true
        binding.btnPetSaveName.isVisible = true
        binding.etPetName.requestFocus()

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etPetName, InputMethodManager.SHOW_IMPLICIT)

        binding.etPetName.setSelection(binding.etPetName.text.length)
    }

    private fun loadPetName() {
        (activity as MainActivity).getPetName(reference) {petName ->
            if (petName != null) {
                binding.petText.text = petName
                binding.etPetName.text = Editable.Factory.getInstance().newEditable(petName)
            }
            else {
                binding.petText.text = R.string.pet_name.toString()
            }
        }
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
        button.isEnabled = false // disabling the button eliminates potential errors when the button is being spammed.
        var newValues = HashMap<String, Any>()
        var newStat by Delegates.notNull<Int>()
        var newCoins by Delegates.notNull<Int>()
        (activity as MainActivity).generateStats(reference, requireContext()) { petHunger, petThirst, petEnjoyment, coins ->
            // check to make sure that coin total is above 5
            if (coins >= 5) {
                // switch case to change what logic is being done for each possible button press
                when (button) {
                    binding.hungerButton -> {
                        if (petHunger >= 100) {
                            statErrorMessage()
                        } else {
                            newStat = petHunger + 15
                            newCoins = coins - 5
                            newValues = hashMapOf(
                                "petHunger" to minOf(newStat, 100),
                                "coins" to newCoins
                            )
                        }
                    }
                    binding.thirstButton -> {
                        if (petThirst >= 100) {
                            statErrorMessage()
                        } else {
                            newStat = petThirst + 15
                            newCoins = coins - 5
                            newValues = hashMapOf(
                                "petThirst" to minOf(newStat, 100),
                                "coins" to newCoins
                            )
                        }
                    }
                    else -> {
                        if (petEnjoyment >= 100) {
                            statErrorMessage()
                        } else{
                            newStat = petEnjoyment + 15
                            newCoins = coins - 5
                            newValues = hashMapOf(
                                "petEnjoyment" to minOf(newStat, 100),
                                "coins" to newCoins
                            )
                        }
                    }
                }
            reference.updateChildren(newValues) // updates the database with the new values
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // successful database update
                        loadPetStats() //loads UI to be in sync with database (or else it would happen to fast)
                        button.isEnabled = true
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
    private fun statErrorMessage() {
        Toast.makeText(
            requireContext(),
            "Stat cannot exceed 100",
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun updatePetName(newName: String) {
        (activity as MainActivity).getPetName(reference) { petName ->
            if (newName.isNotEmpty()) {
                if (petName != newName) {
                    val updatedName = hashMapOf(
                        "petName" to newName
                    )
                    reference.updateChildren(updatedName as Map<String, Any>)
                        .addOnSuccessListener {
                            loadPetName()
                            exitEditing()
                        }
                    }
            }
            else {
                binding.etPetName.text = Editable.Factory.getInstance().newEditable(petName)
                Toast.makeText(
                    requireContext(),
                    "Pet name cannot be blank",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun exitEditing() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etPetName.windowToken, 0)
        binding.etPetName.clearFocus()

        binding.btnPetName.isVisible = true
        binding.btnPetSaveName.isGone = true
    }
}
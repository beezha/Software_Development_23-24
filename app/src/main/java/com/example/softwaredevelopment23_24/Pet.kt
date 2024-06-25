package com.example.softwaredevelopment23_24

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.softwaredevelopment23_24.databinding.FragmentPetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import kotlin.properties.Delegates
import android.os.Handler
import androidx.navigation.fragment.findNavController

// TODO: make UI updates cleaner when points are spent
class Pet : Fragment() {
    private lateinit var binding: FragmentPetBinding
    private lateinit var user: FirebaseUser
    private var database =  FirebaseDatabase.getInstance()
    private lateinit var reference: DatabaseReference
    private lateinit var petwidgetImage: GifImageView
    private lateinit var originalGif: GifDrawable
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sound: SoundPool
    private var soundIDs = HashMap<String, Int>()
    private var loaded = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.avatar_chooser_box, container, false)
        binding = FragmentPetBinding.inflate(inflater, container, false)
        user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        reference = database.reference.child("users").child(userID)
        loadUI() // initial load of the UI

        mediaPlayer = MediaPlayer()

        // Initialize the SoundPool
        sound = SoundPool.Builder()
            .setMaxStreams(1)
            .build()

        // Load all sound files into the SoundPool
        soundIDs["sound1"] = sound.load(requireContext(), R.raw.eating_sound, 1)
        soundIDs["sound2"] = sound.load(requireContext(), R.raw.drinking_sound, 1)
        soundIDs["sound3"] = sound.load(requireContext(), R.raw.fun_sound, 1)

        sound.setOnLoadCompleteListener { _, _, _ ->
            loaded = true
        }

        (activity as MainActivity).getAvatar(reference, requireContext(), view) {
            binding.avatarPetImage.background = it
        }

        binding.avatarPetImage.setOnClickListener{
            findNavController().navigate(R.id.action_Fragment_to_settingsFragment)
        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        petwidgetImage = view.findViewById(R.id.petwidgetImage)
        originalGif = GifDrawable(resources, R.drawable.idle_animation_final)
        petwidgetImage.setImageDrawable(originalGif)
    }

    private fun loadUI() {
        loadPetName()
        loadPetStats()
    }

    private fun startEditing() {
        binding.btnPetName.isGone = true
        binding.btnPetSaveName.isVisible = true
        binding.etPetName.isFocusableInTouchMode = true
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
                            val hungerGif = GifDrawable(resources, R.drawable.eat_animation)

                            // Stop the original GIF and hide it
                            originalGif.stop()


                            // Change the GIF to the new one
                            petwidgetImage.setImageDrawable(hungerGif)
                            hungerGif.start()

                            Handler().postDelayed({
                                mediaPlayer.start()
                                if (loaded) {
                                    sound.play(soundIDs["sound1"]!!, 1.0f, 1.0f, 1, 0, 1.0f)
                                }
                            }, 1000)

                            // Set an animation listener on the new GIF
                            hungerGif.addAnimationListener(object : AnimationListener {
                                override fun onAnimationCompleted(loopNumber: Int) {
                                    // Animation of the new GIF has completed
                                    // Hide the new GIF
                                    petwidgetImage.visibility = View.GONE

                                    // Show the original GIF
                                    petwidgetImage.setImageDrawable(originalGif)
                                    petwidgetImage.visibility = View.VISIBLE
                                    originalGif.start()
                                }
                            })
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
                            val thirstGif = GifDrawable(resources, R.drawable.drink)

                            // Stop the original GIF and hide it
                            originalGif.stop()

                            // Change the GIF to the new one
                            petwidgetImage.setImageDrawable(thirstGif)
                            thirstGif.start()

                            Handler().postDelayed({
                                mediaPlayer.start()
                                if (loaded) {
                                    sound.play(soundIDs["sound2"]!!, 1.0f, 1.0f, 1, 0, 1.0f)
                                }
                            }, 1000)

                            // Set an animation listener on the new GIF
                            thirstGif.addAnimationListener(object : AnimationListener {
                                override fun onAnimationCompleted(loopNumber: Int) {
                                    // Animation of the new GIF has completed
                                    // Hide the new GIF
                                    petwidgetImage.visibility = View.GONE

                                    // Show the original GIF
                                    petwidgetImage.setImageDrawable(originalGif)
                                    petwidgetImage.visibility = View.VISIBLE
                                    originalGif.start()
                                }
                            })
                        }
                    }

                    else -> {
                        if (petEnjoyment >= 100) {
                            statErrorMessage()
                        } else {
                            newStat = petEnjoyment + 15
                            newCoins = coins - 5
                            newValues = hashMapOf(
                                "petEnjoyment" to minOf(newStat, 100),
                                "coins" to newCoins
                            )
                            val enjoymentGif = GifDrawable(resources, R.drawable.play_animation)

                            // Stop the original GIF and hide it
                            originalGif.stop()

                            // Change the GIF to the new one
                            petwidgetImage.setImageDrawable(enjoymentGif)
                            enjoymentGif.start()

                            mediaPlayer.start()
                            if (loaded) {
                                sound.play(soundIDs["sound3"]!!, 1.0f, 1.0f, 1, 0, 1.0f)
                            }

                            // Set an animation listener on the new GIF
                            enjoymentGif.addAnimationListener(object : AnimationListener {
                                override fun onAnimationCompleted(loopNumber: Int) {
                                    // Animation of the new GIF has completed
                                    // Hide the new GIF
                                    petwidgetImage.visibility = View.GONE

                                    // Show the original GIF
                                    petwidgetImage.setImageDrawable(originalGif)
                                    petwidgetImage.visibility = View.VISIBLE
                                    originalGif.start()
                                }
                            })
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
        binding.etPetName.isFocusableInTouchMode = false
    }
}
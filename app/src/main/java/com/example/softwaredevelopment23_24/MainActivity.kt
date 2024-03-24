package com.example.softwaredevelopment23_24

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.softwaredevelopment23_24.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Determine if the user is authenticated and make a boolean value
        val isAuthenticated = FirebaseAuth.getInstance().currentUser != null

        /* This overly long and complicated code is parsing each view id and
            pairing it with the id of the fragments. This is needed
            so the the settings menu item will either go to the login
            fragment or the settings fragment depending on user
            authentication status.
         */
        navView.selectedItemId = R.id.navigation_home

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }

                R.id.navigation_chat -> {
                    navController.navigate(R.id.navigation_chat)
                    true
                }

                R.id.navigation_calendar -> {
                    navController.navigate(R.id.navigation_calendar)
                    true
                }

                R.id.navigation_pet -> {
                    navController.navigate(R.id.navigation_pet)
                    true
                }

                R.id.navigation_settings -> {
                    if (isAuthenticated) {
                        navController.navigate(R.id.navigation_settings_authenticated)
                    } else {
                        navController.navigate(R.id.navigation_settings_unauthenticated)
                    }
                    true
                }
                else -> false
            }
        }
    }
}
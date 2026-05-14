package com.example.moneytracker.presentation.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = destination.id in setOf(
                R.id.onboardingFragment,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.forgotPasswordFragment
            )

            binding.bottomNav.visibility = if (isAuthScreen) View.GONE else View.VISIBLE
            binding.topBar.visibility = if (isAuthScreen) View.GONE else View.VISIBLE
        }
    }
}

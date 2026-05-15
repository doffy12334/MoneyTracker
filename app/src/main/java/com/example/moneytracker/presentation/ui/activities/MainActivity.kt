package com.example.moneytracker.presentation.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.moneytracker.R
import com.example.moneytracker.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

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

        if (savedInstanceState == null && FirebaseAuth.getInstance().currentUser != null) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.dashboardFragment)
            navController.graph = navGraph
        }

        setupBottomNavigation(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = destination.id in setOf(
                R.id.onboardingFragment,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.inputEmailFragment
            )
            if (isAuthScreen) {
                binding.bottomNav.fadeVisibility(View.GONE)
                binding.topBar.fadeVisibility(View.GONE)
            } else {
                binding.bottomNav.fadeVisibility(View.VISIBLE)
                binding.topBar.fadeVisibility(View.VISIBLE)
                if (binding.bottomNav.menu.findItem(destination.id) != null) {
                    binding.bottomNav.menu.findItem(destination.id).isChecked = true
                }
            }
        }
    }

    private fun setupBottomNavigation(navController: NavController) {
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == navController.currentDestination?.id) {
                return@setOnItemSelectedListener true
            }

            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .setPopUpTo(R.id.dashboardFragment, false, true)
                .setRestoreState(true)
                .build()

            runCatching {
                navController.navigate(item.itemId, null, options)
            }.isSuccess
        }
    }

    private fun View.fadeVisibility(targetVisibility: Int) {
        if (visibility == targetVisibility) return
        if (targetVisibility == View.VISIBLE) {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(180L).start()
        } else {
            animate()
                .alpha(0f)
                .setDuration(140L)
                .withEndAction {
                    visibility = View.GONE
                    alpha = 1f
                }
                .start()
        }
    }
}

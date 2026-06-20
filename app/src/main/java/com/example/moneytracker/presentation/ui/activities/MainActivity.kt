package com.example.moneytracker.presentation.ui.activities

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.moneytracker.R
import com.example.moneytracker.databinding.ActivityMainBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.ui.views.ThemeTransitionOverlay

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var shouldLockOnNextStart = false
    private var credentialPromptShowing = false
    private val appLockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        credentialPromptShowing = false
        if (result.resultCode != Activity.RESULT_OK) {
            moveTaskToBack(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (savedInstanceState == null && AppContainer.isUserLoggedInUseCase()) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.dashboardFragment)
            navController.graph = navGraph
        }

        setupBottomNavigation(navController)
        binding.ivpProfile.setOnClickListener {
            if (navController.currentDestination?.id != R.id.profileFragment) {
                navController.navigate(R.id.profileFragment)
            }
        }
        revealPendingThemeTransition()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = destination.id in setOf(
                R.id.onboardingFragment,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.inputEmailFragment,
                R.id.otpVerificationFragment,
                R.id.newPasswordFragment
            )
            val isAccountDetailScreen = destination.id in setOf(
                R.id.profileFragment,
                R.id.securityCenterFragment,
                R.id.exportReportFragment,
                R.id.aboutAppFragment
            )
            if (isAuthScreen) {
                binding.bottomNav.fadeVisibility(View.GONE)
                binding.topBar.fadeVisibility(View.GONE)
            } else if (isAccountDetailScreen) {
                binding.bottomNav.fadeVisibility(View.VISIBLE)
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

    override fun onStart() {
        super.onStart()
        if (shouldLockOnNextStart) {
            shouldLockOnNextStart = false
            requestAppLockIfNeeded()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations &&
            !credentialPromptShowing &&
            AppContainer.isUserLoggedInUseCase()
        ) {
            shouldLockOnNextStart = true
        }
    }

    private fun requestAppLockIfNeeded() {
        if (credentialPromptShowing || !AppContainer.isUserLoggedInUseCase()) return
        if (!AppContainer.getSecuritySettingsUseCase().highValueProtectionEnabled) return

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardSecure) return
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
            getString(R.string.security_app_lock_confirm_title),
            getString(R.string.security_app_lock_confirm_subtitle)
        ) ?: return
        credentialPromptShowing = true
        appLockLauncher.launch(intent)
    }

    fun playThemeTransition(anchor: View, darkMode: Boolean, onCovered: () -> Unit) {
        val decor = window.decorView as ViewGroup
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val originX = location[0] + anchor.width / 2f
        val originY = location[1] + anchor.height / 2f

        pendingThemeTransitionDark = darkMode
        pendingThemeTransitionTime = System.currentTimeMillis()
        pendingThemeTransitionOriginX = originX
        pendingThemeTransitionOriginY = originY

        ThemeTransitionOverlay(this, originX, originY, darkMode).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
            decor.addView(this)
            playCover(onCovered)
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
                .setPopUpTo(R.id.dashboardFragment, false, false)
                .setRestoreState(false)
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

    private fun revealPendingThemeTransition() {
        val darkMode = pendingThemeTransitionDark ?: return
        if (System.currentTimeMillis() - pendingThemeTransitionTime > THEME_TRANSITION_WINDOW_MS) {
            pendingThemeTransitionDark = null
            return
        }

        binding.root.post {
            val decor = window.decorView as ViewGroup
            val overlay = ThemeTransitionOverlay(
                this,
                pendingThemeTransitionOriginX.takeIf { it > 0f } ?: binding.root.width * 0.74f,
                pendingThemeTransitionOriginY.takeIf { it > 0f } ?: binding.root.height * 0.36f,
                darkMode,
                startsCovered = true
            ).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                alpha = 1f
                isClickable = true
            }
            decor.addView(overlay)
            overlay.playReveal {
                pendingThemeTransitionDark = null
                pendingThemeTransitionOriginX = 0f
                pendingThemeTransitionOriginY = 0f
            }
        }
    }

    companion object {
        private const val THEME_TRANSITION_WINDOW_MS = 2_000L
        private var pendingThemeTransitionDark: Boolean? = null
        private var pendingThemeTransitionTime: Long = 0L
        private var pendingThemeTransitionOriginX: Float = 0f
        private var pendingThemeTransitionOriginY: Float = 0f
    }
}

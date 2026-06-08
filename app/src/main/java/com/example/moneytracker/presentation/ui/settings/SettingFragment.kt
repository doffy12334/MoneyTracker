package com.example.moneytracker.presentation.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentSettingBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.domain.model.AppLanguage
import com.example.moneytracker.domain.model.AppTheme
import com.example.moneytracker.presentation.ui.activities.MainActivity
import com.example.moneytracker.presentation.uistate.SettingsUiState
import com.example.moneytracker.presentation.viewmodel.SettingsViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            AppContainer.getSettingsUseCase,
            AppContainer.setNotificationsEnabledUseCase,
            AppContainer.setLanguageUseCase,
            AppContainer.setThemeUseCase,
            AppContainer.setCurrencyUseCase,
            AppContainer.refreshExchangeRatesUseCase,
            AppContainer.logoutUseCase
        )
    }

    private var appliedLanguage: AppLanguage? = null
    private var appliedTheme: AppTheme? = null
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            viewModel.onNotificationsChanged(false)
        }
    }

    private val switchNotification: MaterialSwitch
        get() = binding.root.findViewById(R.id.switchNotification)
    private val rowLanguage: View
        get() = binding.root.findViewById(R.id.rowLanguage)
    private val rowTheme: View
        get() = binding.root.findViewById(R.id.rowTheme)
    private val rowCurrency: View
        get() = binding.root.findViewById(R.id.rowCurrency)
    private val rowProfile: View
        get() = binding.root.findViewById(R.id.rowProfile)
    private val rowSecurity: View
        get() = binding.root.findViewById(R.id.rowSecurity)
    private val rowExportData: View
        get() = binding.root.findViewById(R.id.rowExportData)
    private val rowDeleteAccount: View
        get() = binding.root.findViewById(R.id.rowDeleteAccount)
    private val rowAboutApp: View
        get() = binding.root.findViewById(R.id.rowAboutApp)
    private val rowTerms: View
        get() = binding.root.findViewById(R.id.rowTerms)
    private val tvLanguageValue: TextView
        get() = binding.root.findViewById(R.id.tvLanguageValue)
    private val tvThemeValue: TextView
        get() = binding.root.findViewById(R.id.tvThemeValue)
    private val tvCurrencyValue: TextView
        get() = binding.root.findViewById(R.id.tvCurrencyValue)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onNotificationsChanged(isChecked)
            if (isChecked) requestNotificationPermissionIfNeeded()
        }
        rowLanguage.setSmoothClickListener {
            showLanguageDialog()
        }
        rowTheme.setSmoothClickListener {
            showThemeDialog()
        }
        rowCurrency.setSmoothClickListener {
            showCurrencyDialog()
        }
        rowProfile.setSmoothClickListener {
            navigateSettingDetail(R.id.profileFragment)
        }
        rowSecurity.setSmoothClickListener {
            navigateSettingDetail(R.id.securityCenterFragment)
        }
        rowExportData.setSmoothClickListener {
            navigateSettingDetail(R.id.exportReportFragment)
        }
        rowDeleteAccount.setSmoothClickListener {
            showDeleteAccountDialog()
        }
        rowAboutApp.setSmoothClickListener {
            navigateSettingDetail(R.id.aboutAppFragment)
        }
        rowTerms.setSmoothClickListener {
            showTermsDialog()
        }
        binding.btnLogout.setOnClickListener {
            logout()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: SettingsUiState) {
        if (switchNotification.isChecked != state.notificationsEnabled) {
            switchNotification.isChecked = state.notificationsEnabled
        }

        tvLanguageValue.text = languageLabel(state.language)
        tvThemeValue.text = themeLabel(state.theme)
        tvCurrencyValue.text = currencyLabel(state.currency)
        applyLanguage(state.language)
        applyTheme(state.theme)
    }

    private fun showLanguageDialog() {
        val languages = AppLanguage.entries.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_language_title))
            .setItems(languages.map { languageLabel(it) }.toTypedArray()) { _, index ->
                viewModel.onLanguageChanged(languages[index])
            }
            .show()
    }

    private fun showCurrencyDialog() {
        val currencies = AppCurrency.entries.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_currency_title))
            .setItems(currencies.map { currencyLabel(it) }.toTypedArray()) { _, index ->
                viewModel.onCurrencyChanged(currencies[index])
            }
            .show()
    }

    private fun showThemeDialog() {
        val themes = AppTheme.entries.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_theme_title))
            .setItems(themes.map { themeLabel(it) }.toTypedArray()) { dialog, index ->
                dialog.dismiss()
                rowTheme.postDelayed({
                    viewModel.onThemeChanged(themes[index])
                }, THEME_DIALOG_DISMISS_DELAY_MS)
            }
            .show()
    }

    private fun logout() {
        viewModel.logout()
        Toast.makeText(requireContext(), getString(R.string.text_logout), Toast.LENGTH_SHORT).show()

        navigateToLogin()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun showDeleteAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account_confirm, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            dialog.dismiss()
            showDeleteAccountPasswordDialog()
        }
        dialog.showThemed()
    }

    private fun showTermsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.about_terms_title)
            .setMessage(R.string.about_terms_message)
            .setPositiveButton(R.string.action_ok, null)
            .show()
    }

    private fun showDeleteAccountPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account_password, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.etPassword)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val password = passwordInput.text.toString()
            if (password.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    R.string.delete_account_password_required,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            deleteAccount(password)
        }
        dialog.showThemed()
    }

    private fun deleteAccount(password: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            rowDeleteAccount.isEnabled = false
            Toast.makeText(requireContext(), R.string.delete_account_deleting, Toast.LENGTH_SHORT).show()
            runCatching {
                AppContainer.deleteAccountUseCase(password)
            }.onSuccess {
                Toast.makeText(requireContext(), R.string.delete_account_success, Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }.onFailure { throwable ->
                rowDeleteAccount.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    throwable.message ?: getString(R.string.delete_account_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(
            R.id.loginFragment,
            null,
            NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    private fun AlertDialog.showThemed() {
        show()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun navigateSettingDetail(destinationId: Int) {
        findNavController().navigate(
            destinationId,
            null,
            NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()
        )
    }

    private fun View.setSmoothClickListener(action: () -> Unit) {
        setOnClickListener {
            if (!isEnabled) return@setOnClickListener
            isEnabled = false
            animate()
                .scaleX(SETTING_ITEM_PRESSED_SCALE)
                .scaleY(SETTING_ITEM_PRESSED_SCALE)
                .alpha(SETTING_ITEM_PRESSED_ALPHA)
                .setDuration(SETTING_ITEM_PRESS_IN_MS)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(SETTING_ITEM_PRESS_OUT_MS)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction {
                            isEnabled = true
                            action()
                        }
                        .start()
                }
                .start()
        }
    }

    private fun applyLanguage(language: AppLanguage) {
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (appliedLanguage == language || currentTags == language.code) {
            appliedLanguage = language
            return
        }
        appliedLanguage = language
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code)
        )
    }

    private fun applyTheme(theme: AppTheme) {
        val nightMode = when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        if (appliedTheme == theme || AppCompatDelegate.getDefaultNightMode() == nightMode) {
            appliedTheme = theme
            return
        }
        appliedTheme = theme
        val mainActivity = activity as? MainActivity
        if (mainActivity == null || !isResumed) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
            return
        }
        mainActivity.playThemeTransition(rowTheme, theme == AppTheme.DARK) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    private fun languageLabel(language: AppLanguage): String {
        return getString(
            when (language) {
                AppLanguage.VIETNAMESE -> R.string.language_vietnamese
                AppLanguage.ENGLISH -> R.string.language_english
                AppLanguage.FRENCH -> R.string.language_french
            }
        )
    }

    private fun themeLabel(theme: AppTheme): String {
        return getString(
            when (theme) {
                AppTheme.LIGHT -> R.string.theme_light
                AppTheme.DARK -> R.string.theme_dark
            }
        )
    }

    private fun currencyLabel(currency: AppCurrency): String {
        return getString(
            when (currency) {
                AppCurrency.VND -> R.string.currency_vnd
                AppCurrency.USD -> R.string.currency_usd
                AppCurrency.EUR -> R.string.currency_eur
                AppCurrency.JPY -> R.string.currency_jpy
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val THEME_DIALOG_DISMISS_DELAY_MS = 90L
        private const val SETTING_ITEM_PRESSED_SCALE = 0.97f
        private const val SETTING_ITEM_PRESSED_ALPHA = 0.86f
        private const val SETTING_ITEM_PRESS_IN_MS = 70L
        private const val SETTING_ITEM_PRESS_OUT_MS = 120L
    }
}

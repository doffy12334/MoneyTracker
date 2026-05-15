package com.example.moneytracker.presentation.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
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
import com.example.moneytracker.domain.model.AppLanguage
import com.example.moneytracker.domain.model.AppTheme
import com.example.moneytracker.presentation.uistate.SettingsUiState
import com.example.moneytracker.presentation.viewmodel.SettingsViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            AppContainer.getSettingsUseCase,
            AppContainer.setNotificationsEnabledUseCase,
            AppContainer.setLanguageUseCase,
            AppContainer.setThemeUseCase
        )
    }

    private var appliedLanguage: AppLanguage? = null
    private var appliedTheme: AppTheme? = null

    private val switchNotification: MaterialSwitch
        get() = binding.root.findViewById(R.id.switchNotification)
    private val rowLanguage: View
        get() = binding.root.findViewById(R.id.rowLanguage)
    private val rowTheme: View
        get() = binding.root.findViewById(R.id.rowTheme)
    private val tvLanguageValue: TextView
        get() = binding.root.findViewById(R.id.tvLanguageValue)
    private val tvThemeValue: TextView
        get() = binding.root.findViewById(R.id.tvThemeValue)

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
        }
        rowLanguage.setOnClickListener {
            showLanguageDialog()
        }
        rowTheme.setOnClickListener {
            showThemeDialog()
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

        tvLanguageValue.text = state.language.displayName
        tvThemeValue.text = state.theme.displayName
        applyLanguage(state.language)
        applyTheme(state.theme)
    }

    private fun showLanguageDialog() {
        val languages = AppLanguage.entries.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Ngôn ngữ")
            .setItems(languages.map { it.displayName }.toTypedArray()) { _, index ->
                viewModel.onLanguageChanged(languages[index])
            }
            .show()
    }

    private fun showThemeDialog() {
        val themes = AppTheme.entries.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Giao diện")
            .setItems(themes.map { it.displayName }.toTypedArray()) { _, index ->
                viewModel.onThemeChanged(themes[index])
            }
            .show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()

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
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

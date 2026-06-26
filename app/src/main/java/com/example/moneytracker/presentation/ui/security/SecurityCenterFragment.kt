package com.example.moneytracker.presentation.ui.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentSecurityCenterBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.ui.auth.NewPasswordFragment
import com.example.moneytracker.presentation.uistate.SecurityCenterUiState
import com.example.moneytracker.presentation.viewmodel.SecurityCenterViewModel
import kotlinx.coroutines.launch

class SecurityCenterFragment : Fragment() {
    private var _binding: FragmentSecurityCenterBinding? = null
    private val binding get() = _binding!!
    private var isRendering = false

    private val biometricEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onBiometricChanged(result.resultCode == Activity.RESULT_OK)
        if (result.resultCode != Activity.RESULT_OK) {
            Toast.makeText(requireContext(), R.string.security_biometric_cancelled, Toast.LENGTH_SHORT).show()
        }
    }
    private val appLockEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onHighValueProtectionChanged(result.resultCode == Activity.RESULT_OK)
        if (result.resultCode != Activity.RESULT_OK) {
            Toast.makeText(requireContext(), R.string.security_app_lock_cancelled, Toast.LENGTH_SHORT).show()
        }
    }

    private val viewModel: SecurityCenterViewModel by viewModels {
        SecurityCenterViewModel.Factory(
            AppContainer.getSecuritySettingsUseCase,
            AppContainer.setBiometricEnabledUseCase,
            AppContainer.setHighValueProtectionEnabledUseCase,
            AppContainer.getProfileUseCase,
            AppContainer.updatePasswordUseCase,
            AppContainer.logoutUseCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.rowChangePassword.setOnClickListener {
            val args = Bundle().apply {
                putBoolean(NewPasswordFragment.ARG_IS_FROM_SECURITY_CENTER, true)
            }
            findNavController().navigate(R.id.action_securityCenter_to_newPassword, args)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (!isRendering) {
                if (isChecked) {
                    requestBiometricConfirmation()
                } else {
                    viewModel.onBiometricChanged(false)
                }
            }
        }
        binding.switchHighValue.setOnCheckedChangeListener { _, isChecked ->
            if (!isRendering) {
                if (isChecked) {
                    requestAppLockConfirmation()
                } else {
                    viewModel.onHighValueProtectionChanged(false)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.switchBiometric.setOnCheckedChangeListener(null)
        binding.switchHighValue.setOnCheckedChangeListener(null)
    }

    private fun renderState(state: SecurityCenterUiState) {
        isRendering = true
        binding.switchBiometric.isChecked = state.biometricEnabled
        binding.switchHighValue.isChecked = state.highValueProtectionEnabled
        isRendering = false

        val message = state.errorMessage
            ?: state.messageResId?.let { getString(it) }
            ?: state.message
        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
        if (state.shouldLogoutAfterPasswordReset) {
            viewModel.consumeLogoutEvent()
            navigateToLogin()
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

    private fun requestBiometricConfirmation() {
        val keyguardManager = requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardSecure) {
            viewModel.onBiometricChanged(false)
            Toast.makeText(requireContext(), R.string.security_biometric_not_available, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
            getString(R.string.security_biometric_confirm_title),
            getString(R.string.security_biometric_confirm_subtitle)
        )
        if (intent == null) {
            viewModel.onBiometricChanged(false)
            Toast.makeText(requireContext(), R.string.security_biometric_not_available, Toast.LENGTH_SHORT).show()
            return
        }
        biometricEnableLauncher.launch(intent)
    }

    private fun requestAppLockConfirmation() {
        val keyguardManager = requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardSecure) {
            viewModel.onHighValueProtectionChanged(false)
            Toast.makeText(requireContext(), R.string.security_app_lock_not_available, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
            getString(R.string.security_app_lock_confirm_title),
            getString(R.string.security_app_lock_confirm_subtitle)
        )
        if (intent == null) {
            viewModel.onHighValueProtectionChanged(false)
            Toast.makeText(requireContext(), R.string.security_app_lock_not_available, Toast.LENGTH_SHORT).show()
            return
        }
        appLockEnableLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

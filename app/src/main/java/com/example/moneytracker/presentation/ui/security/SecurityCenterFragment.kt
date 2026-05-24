package com.example.moneytracker.presentation.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.moneytracker.presentation.uistate.SecurityCenterUiState
import com.example.moneytracker.presentation.viewmodel.SecurityCenterViewModel
import kotlinx.coroutines.launch

class SecurityCenterFragment : Fragment() {
    private var _binding: FragmentSecurityCenterBinding? = null
    private val binding get() = _binding!!
    private var isRendering = false

    private val viewModel: SecurityCenterViewModel by viewModels {
        SecurityCenterViewModel.Factory(
            AppContainer.getSecuritySettingsUseCase,
            AppContainer.setTwoFactorEnabledUseCase,
            AppContainer.setBiometricEnabledUseCase,
            AppContainer.setHighValueProtectionEnabledUseCase,
            AppContainer.getProfileUseCase,
            AppContainer.sendPasswordResetEmailUseCase,
            AppContainer.isCurrentUserGoogleAccountUseCase,
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
        binding.rowChangePassword.setOnClickListener { viewModel.sendChangePasswordEmail() }
        binding.btnConfirmNewPassword.setOnClickListener {
            viewModel.updatePassword(
                binding.etNewPassword.text.toString(),
                binding.etConfirmNewPassword.text.toString()
            )
        }
        binding.switchTwoFactor.setOnCheckedChangeListener { _, isChecked ->
            if (!isRendering) viewModel.onTwoFactorChanged(isChecked)
        }
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (!isRendering) viewModel.onBiometricChanged(isChecked)
        }
        binding.switchHighValue.setOnCheckedChangeListener { _, isChecked ->
            if (!isRendering) viewModel.onHighValueProtectionChanged(isChecked)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: SecurityCenterUiState) {
        isRendering = true
        binding.switchTwoFactor.isChecked = state.twoFactorEnabled
        binding.switchBiometric.isChecked = state.biometricEnabled
        binding.switchHighValue.isChecked = state.highValueProtectionEnabled
        isRendering = false
        renderPasswordResetLoading(state.isPasswordResetLoading)
        renderPasswordForm(state)

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

    private fun renderPasswordResetLoading(isLoading: Boolean) {
        binding.rowChangePassword.isEnabled = !isLoading
        binding.btnConfirmNewPassword.isEnabled = !isLoading
        binding.etNewPassword.isEnabled = !isLoading
        binding.etConfirmNewPassword.isEnabled = !isLoading
        binding.rowChangePassword.alpha = if (isLoading) 0.72f else 1f
        binding.progressChangePassword.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.ivChangePasswordArrow.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.tvChangePasswordSubtitle.text = getString(
            if (isLoading) R.string.security_password_reset_sending else R.string.security_change_password_subtitle
        )
    }

    private fun renderPasswordForm(state: SecurityCenterUiState) {
        binding.layoutNewPassword.visibility = if (state.isPasswordFormVisible) View.VISIBLE else View.GONE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

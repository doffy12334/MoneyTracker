package com.example.moneytracker.presentation.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentNewPasswordBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.uistate.NewPasswordUiState
import com.example.moneytracker.presentation.viewmodel.NewPasswordViewModel
import kotlinx.coroutines.launch

class NewPasswordFragment : Fragment() {
    private var _binding: FragmentNewPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewPasswordViewModel by viewModels {
        AppContainer.newPasswordViewModelFactory
    }

    private lateinit var userEmail: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userEmail = requireArguments().getString(ARG_USER_EMAIL, "")
        val isFromSecurityCenter = requireArguments().getBoolean(ARG_IS_FROM_SECURITY_CENTER, false)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnResetPassword.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            viewModel.resetPassword(userEmail, newPassword, confirmPassword, isFromSecurityCenter)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: NewPasswordUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.btnResetPassword.isEnabled = !state.isLoading
        binding.etNewPassword.isEnabled = !state.isLoading
        binding.etConfirmPassword.isEnabled = !state.isLoading

        state.errorMessage?.let {
            Toast.makeText(context, getString(it), Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }

        if (state.isAutoLoginSuccessful) {
            Toast.makeText(
                context,
                getString(R.string.password_reset_done),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetNavigation()
            findNavController().navigate(R.id.action_newPassword_to_dashboard)
        }

        if (state.isPasswordUpdatedAndLoggedOut) {
            Toast.makeText(
                context,
                getString(R.string.security_password_update_success),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetNavigation()
            findNavController().navigate(R.id.action_newPassword_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_USER_EMAIL = "userEmail"
        const val ARG_IS_FROM_SECURITY_CENTER = "isFromSecurityCenter"
    }
}

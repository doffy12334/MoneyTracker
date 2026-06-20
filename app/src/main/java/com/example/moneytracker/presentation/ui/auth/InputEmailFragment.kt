package com.example.moneytracker.presentation.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentInputEmailBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.uistate.InputEmailUiState
import com.example.moneytracker.presentation.viewmodel.InputEmailViewModel
import kotlinx.coroutines.launch

class InputEmailFragment : Fragment() {
    private var _binding: FragmentInputEmailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InputEmailViewModel by viewModels {
        InputEmailViewModel.Factory(
            AppContainer.sendPasswordResetEmailUseCase,
            AppContainer.sendPhoneOtpUseCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.etInput.addTextChangedListener {
            viewModel.onInputChanged(it.toString().trim())
        }
        binding.btnConfirm.setOnClickListener {
            viewModel.sendResetCode(requireActivity())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: InputEmailUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.btnConfirm.isEnabled = !state.isLoading

        val errorMsg = if (state.errorMessageResId == R.string.error_unknown && state.errorMessage != null) {
            state.errorMessage
        } else {
            state.errorMessageResId?.let { getString(it) } ?: state.errorMessage
        }
        
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }

        // Email flow: show toast and pop back to login
        if (state.isEmailSent) {
            Toast.makeText(
                context,
                getString(R.string.email_reset_sent),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetNavigation()
            findNavController().popBackStack(R.id.loginFragment, false)
        }

        // Phone flow: navigate to OTP verification
        if (state.isCodeSent && state.verificationId != null) {
            val bundle = bundleOf(
                OtpVerificationFragment.ARG_VERIFICATION_ID to state.verificationId,
                OtpVerificationFragment.ARG_PHONE_NUMBER to state.input,
                OtpVerificationFragment.ARG_FLOW_TYPE to OtpVerificationFragment.FLOW_FORGOT_PASSWORD
            )
            viewModel.resetNavigation()
            findNavController().navigate(
                R.id.action_inputEmail_to_otpVerification,
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

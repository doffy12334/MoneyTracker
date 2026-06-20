package com.example.moneytracker.presentation.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentOtpVerificationBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.uistate.OtpVerificationUiState
import com.example.moneytracker.presentation.viewmodel.OtpVerificationViewModel
import kotlinx.coroutines.launch

class OtpVerificationFragment : Fragment() {
    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OtpVerificationViewModel by viewModels {
        AppContainer.otpVerificationViewModelFactory
    }

    private lateinit var phoneNumber: String
    private lateinit var flowType: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val verificationId = requireArguments().getString(ARG_VERIFICATION_ID, "")
        phoneNumber = requireArguments().getString(ARG_PHONE_NUMBER, "")
        flowType = requireArguments().getString(ARG_FLOW_TYPE, FLOW_FORGOT_PASSWORD)

        viewModel.initialize(verificationId)

        binding.tvSubtitle.text = getString(R.string.otp_subtitle, maskPhone(phoneNumber))

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnVerify.setOnClickListener {
            val code = binding.pinViewOtp.text?.toString() ?: ""
            if (code.length == 6) {
                when (flowType) {
                    FLOW_FORGOT_PASSWORD -> viewModel.verifyOtp(code)
                    FLOW_REGISTRATION -> viewModel.linkPhone(code)
                }
            } else {
                Toast.makeText(context, getString(R.string.error_invalid_otp), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnResend.setOnClickListener {
            viewModel.resendOtp(phoneNumber, requireActivity())
        }

        binding.pinViewOtp.requestFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(binding.pinViewOtp, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: OtpVerificationUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.btnVerify.isEnabled = !state.isLoading
        binding.pinViewOtp.isEnabled = !state.isLoading

        // Countdown
        val counting = state.resendCountdown > 0
        binding.tvCountdown.isVisible = counting
        binding.btnResend.isEnabled = !counting && !state.isLoading
        if (counting) {
            binding.tvCountdown.text = getString(R.string.resend_countdown, state.resendCountdown)
        }

        state.errorMessage?.let {
            Toast.makeText(context, getString(it), Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }

        // Forgot password flow → navigate to new password screen
        if (state.isVerified) {
            viewModel.resetNavigation()
            findNavController().navigate(
                R.id.action_otpVerification_to_newPassword,
                bundleOf(NewPasswordFragment.ARG_USER_EMAIL to getUserEmail())
            )
        }

        // Registration flow → phone linked, go to dashboard
        if (state.isPhoneLinked) {
            viewModel.resetNavigation()
            Toast.makeText(context, getString(R.string.phone_linked_success), Toast.LENGTH_SHORT)
                .show()
            findNavController().navigate(R.id.action_otpVerification_to_dashboard)
        }
    }

    private fun getUserEmail(): String {
        // After phone verification signIn, the current user's email may be available
        // We pass it through so NewPasswordFragment can auto-login
        return AppContainer.authRepository.getCurrentUserEmail() ?: ""
    }

    private fun maskPhone(phone: String): String {
        return if (phone.length > 4) {
            phone.substring(0, phone.length - 4).replace(Regex("[0-9]"), "*") +
                    phone.substring(phone.length - 4)
        } else {
            phone
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_VERIFICATION_ID = "verificationId"
        const val ARG_PHONE_NUMBER = "phoneNumber"
        const val ARG_FLOW_TYPE = "flowType"
        const val FLOW_FORGOT_PASSWORD = "FORGOT_PASSWORD"
        const val FLOW_REGISTRATION = "REGISTRATION"
    }
}

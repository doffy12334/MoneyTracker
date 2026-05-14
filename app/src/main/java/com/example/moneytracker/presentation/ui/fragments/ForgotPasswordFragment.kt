package com.example.moneytracker.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.databinding.FragmentForgotPasswordBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.uistate.ForgotPasswordUiState
import com.example.moneytracker.presentation.viewmodel.ForgotPasswordViewModel
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForgotPasswordViewModel by viewModels {
        ForgotPasswordViewModel.Factory(
            AppContainer.sendPasswordResetEmailUseCase,
            AppContainer.verifyPasswordResetCodeUseCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etEmail.addTextChangedListener { text ->
            viewModel.onEmailChanged(text.toString().trim())
        }

        binding.tvResend.setOnClickListener {
            viewModel.resendEmail()
        }

        binding.otpEdit.addTextChangedListener { text ->
            viewModel.onResetCodeChanged(text.toString())
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.verifyResetCode()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: ForgotPasswordUiState) {
        binding.tvResend.isEnabled = state.canResend && !state.isLoading
        binding.tvResend.alpha = if (state.canResend) ENABLED_ALPHA else DISABLED_ALPHA
        binding.tvTimer.text = if (state.canResend) {
            "Ban co the gui email khoi phuc"
        } else {
            "Gui lai email sau ${state.remainingSeconds}s"
        }

        binding.btnConfirm.isEnabled = !state.isLoading

        if (state.isEmailSent) {
            binding.tvSubHeader.text = "Email khoi phuc mat khau da duoc gui. Hay mo email, lay ma oobCode trong link va nhap vao o ma xac thuc."
        }

        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        if (state.isCodeVerified) {
            Toast.makeText(
                context,
                "Ma hop le cho email: ${state.verifiedEmail.orEmpty()}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val ENABLED_ALPHA = 1.0f
        const val DISABLED_ALPHA = 0.5f
    }
}

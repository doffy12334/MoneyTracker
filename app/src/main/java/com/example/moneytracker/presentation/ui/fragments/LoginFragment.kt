package com.example.moneytracker.presentation.ui.fragments

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentLoginBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.uistate.LoginUiState
import com.example.moneytracker.presentation.ui.views.PigLoginView.PigState
import com.example.moneytracker.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var isPasswordVisible = false
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(AppContainer.loginUseCase)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPasswordPeekAnimation()

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                email = binding.etEmail.text.toString().trim(),
                password = binding.etPassword.text.toString().trim()
            )
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_inputEmailFragment)
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun setupPasswordPeekAnimation() {
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.pigLoginView.setPigState(PigState.NORMAL)
            }
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            binding.pigLoginView.setPigState(
                when {
                    hasFocus && isPasswordVisible -> PigState.PEEKING
                    hasFocus -> PigState.COVERED
                    else -> PigState.NORMAL
                }
            )
        }

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            renderPasswordVisibility()
            binding.pigLoginView.setPigState(
                if (isPasswordVisible) PigState.PEEKING else PigState.COVERED
            )
        }
    }

    private fun renderPasswordVisibility() {
        val selection = binding.etPassword.selectionEnd.coerceAtLeast(0)
        binding.etPassword.transformationMethod = if (isPasswordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        binding.etPassword.setSelection(selection.coerceAtMost(binding.etPassword.text.length))
        binding.btnTogglePassword.setImageResource(
            if (isPasswordVisible) R.drawable.ic_visibility_24 else R.drawable.ic_visibility_off_24
        )
        binding.btnTogglePassword.contentDescription = getString(
            if (isPasswordVisible) R.string.hide_password else R.string.show_password
        )
    }

    private fun renderState(state: LoginUiState) {
        binding.btnLogin.isEnabled = state !is LoginUiState.Loading
        when (state) {
            LoginUiState.Idle,
            LoginUiState.Loading -> Unit

            LoginUiState.Authenticated -> {
                findNavController().navigate(R.id.action_login_to_dashboard)
            }

            is LoginUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

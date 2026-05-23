package com.example.moneytracker.presentation.ui.auth

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentLoginBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.ui.views.PigLoginView
import com.example.moneytracker.presentation.uistate.LoginUiState
import com.example.moneytracker.presentation.viewmodel.LoginViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var isPasswordVisible = false
    private lateinit var credentialManager: CredentialManager
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(
            AppContainer.loginUseCase,
            AppContainer.loginWithGoogleUseCase
        )
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
        credentialManager = CredentialManager.Companion.create(requireContext())

        setupPasswordPeekAnimation()
        setupGoogleSignIn()

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString()
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

    private fun setupGoogleSignIn() {
        binding.tvContinue.visibility = View.VISIBLE
        binding.btnGoogle.visibility = View.VISIBLE
        binding.btnGoogle.setOnClickListener {
            launchGoogleSignIn()
        }
    }

    private fun launchGoogleSignIn() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.btnGoogle.isEnabled = false
            try {
                val idToken = getGoogleIdToken(filterByAuthorizedAccounts = true)
                    ?: getGoogleIdToken(filterByAuthorizedAccounts = false)
                if (idToken == null) {
                    viewModel.setError("Không lấy được tài khoản Google")
                } else {
                    viewModel.loginWithGoogle(idToken)
                }
            } catch (exception: GoogleIdTokenParsingException) {
                viewModel.setError("Không đọc được thông tin tài khoản Google")
            } catch (exception: GetCredentialException) {
                viewModel.setError("Đăng nhập Google bị hủy hoặc thất bại")
            } finally {
                binding.btnGoogle.isEnabled = true
            }
        }
    }

    private suspend fun getGoogleIdToken(filterByAuthorizedAccounts: Boolean): String? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = requireContext(),
                request = request
            )
            val credential = result.credential
            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } else {
                null
            }
        } catch (exception: NoCredentialException) {
            null
        }
    }

    private fun setupPasswordPeekAnimation() {
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.pigLoginView.setPigState(PigLoginView.PigState.NORMAL)
            }
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            binding.pigLoginView.setPigState(
                when {
                    hasFocus && isPasswordVisible -> PigLoginView.PigState.PEEKING
                    hasFocus -> PigLoginView.PigState.COVERED
                    else -> PigLoginView.PigState.NORMAL
                }
            )
        }

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            renderPasswordVisibility()
            binding.pigLoginView.setPigState(
                if (isPasswordVisible) PigLoginView.PigState.PEEKING else PigLoginView.PigState.COVERED
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
        val isLoading = state is LoginUiState.Loading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.btnTogglePassword.isEnabled = !isLoading
        binding.tvForgotPassword.isEnabled = !isLoading
        binding.tvSignUp.isEnabled = !isLoading
        binding.btnGoogle.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "" else getString(R.string.log_in)

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

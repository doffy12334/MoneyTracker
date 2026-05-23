package com.example.moneytracker.presentation.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        InputEmailViewModel.Factory(AppContainer.sendPasswordResetEmailUseCase)
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
        binding.etEmail.addTextChangedListener {
            viewModel.onEmailChanged(it.toString().trim())
        }
        binding.btnConfirm.setOnClickListener {
            viewModel.sendResetEmail()
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

        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        if (state.isEmailSent) {
            Toast.makeText(
                context,
                "Email khôi phục mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư.",
                Toast.LENGTH_LONG
            ).show()
            findNavController().popBackStack(R.id.loginFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

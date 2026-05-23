package com.example.moneytracker.presentation.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.databinding.FragmentProfileBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.uistate.ProfileUiState
import com.example.moneytracker.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var isRendering = false
    private var hasTriedToSave = false

    private val avatarPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            persistAvatarPermission(uri)
            viewModel.onAvatarChanged(uri.toString())
        }
    }

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.Factory(
            AppContainer.getProfileUseCase,
            AppContainer.updateProfileUseCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.ivAvatar.setOnClickListener { openAvatarPicker() }
        binding.btnEditAvatar.setOnClickListener { openAvatarPicker() }
        binding.btnSaveProfile.setOnClickListener {
            hasTriedToSave = true
            if (renderValidationErrors()) {
                viewModel.saveProfile()
            } else {
                Toast.makeText(requireContext(), "Vui long kiem tra email va so dien thoai", Toast.LENGTH_SHORT).show()
            }
        }

        val textWatcher: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ ->
            if (!isRendering) {
                viewModel.onProfileChanged(
                    fullName = binding.etFullName.text.toString(),
                    email = binding.etEmail.text.toString(),
                    phone = binding.etPhone.text.toString(),
                    birthday = binding.etBirthday.text.toString()
                )
                if (hasTriedToSave) renderValidationErrors()
            }
        }
        binding.etFullName.addTextChangedListener(onTextChanged = textWatcher)
        binding.etEmail.addTextChangedListener(onTextChanged = textWatcher)
        binding.etPhone.addTextChangedListener(onTextChanged = textWatcher)
        binding.etBirthday.addTextChangedListener(onTextChanged = textWatcher)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: ProfileUiState) {
        isRendering = true
        updateTextIfChanged(binding.etFullName, state.fullName)
        updateTextIfChanged(binding.etEmail, state.email)
        updateTextIfChanged(binding.etPhone, state.phone)
        updateTextIfChanged(binding.etBirthday, state.birthday)
        renderAvatar(state.avatarUri)
        isRendering = false
        binding.btnSaveProfile.isEnabled = !state.isSaving && !state.isLoading
        binding.btnSaveProfile.text = if (state.isSaving) "Dang luu..." else "Luu thay doi"
        if (hasTriedToSave) renderValidationErrors()

        state.errorMessage?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            viewModel.consumeSaveResult()
        }
        if (state.isSaved) {
            Toast.makeText(
                requireContext(),
                state.successMessage ?: "Da luu ho so",
                Toast.LENGTH_LONG
            ).show()
            viewModel.consumeSaveResult()
        }
    }

    private fun renderAvatar(avatarUri: String) {
        if (avatarUri.isBlank()) {
            binding.ivAvatar.setImageResource(com.example.moneytracker.R.drawable.ic_profile)
            binding.ivAvatar.setColorFilter(ContextCompat.getColor(requireContext(), com.example.moneytracker.R.color.primary_color))
        } else {
            binding.ivAvatar.clearColorFilter()
            binding.ivAvatar.setImageURI(Uri.parse(avatarUri))
        }
    }

    private fun openAvatarPicker() {
        avatarPicker.launch(arrayOf("image/*"))
    }

    private fun persistAvatarPermission(uri: Uri) {
        runCatching {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun renderValidationErrors(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val birthday = binding.etBirthday.text.toString().trim()

        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPhoneValid = phone.isBlank() || PHONE_REGEX.matches(phone)
        val isBirthdayValid = birthday.isBlank() || BIRTHDAY_REGEX.matches(birthday)

        binding.etFullName.error = if (fullName.isBlank()) "Ho ten khong duoc de trong" else null
        binding.etEmail.error = if (isEmailValid) null else "Email khong hop le"
        binding.etPhone.error = if (isPhoneValid) null else "So dien thoai khong hop le"
        binding.etBirthday.error = if (isBirthdayValid) null else "Ngay sinh phai co dinh dang MM/DD/YYYY"

        return fullName.isNotBlank() && isEmailValid && isPhoneValid && isBirthdayValid
    }

    private fun updateTextIfChanged(editText: android.widget.EditText, value: String) {
        if (editText.text.toString() != value) {
            editText.setText(value)
            editText.setSelection(value.length)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        val PHONE_REGEX = Regex("^[+0-9][0-9 ]{7,18}$")
        val BIRTHDAY_REGEX = Regex("^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$")
    }
}

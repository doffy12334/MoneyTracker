package com.example.moneytracker.presentation.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.moneytracker.R
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
        binding.ivAvatar.setOnClickListener { openAvatarPickerIfEditing() }
        binding.btnEditAvatar.setOnClickListener { openAvatarPickerIfEditing() }
        binding.btnEditProfile.setOnClickListener {
            hasTriedToSave = false
            viewModel.startEditing()
        }
        binding.btnSaveProfile.setOnClickListener {
            hasTriedToSave = true
            if (renderValidationErrors()) {
                viewModel.saveProfile()
            } else {
                Toast.makeText(requireContext(), getString(R.string.profile_validation_check), Toast.LENGTH_SHORT).show()
            }
        }

        val textWatcher: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ ->
            if (!isRendering) {
                viewModel.onProfileChanged(
                    fullName = binding.etFullName.text.toString(),
                    email = viewModel.uiState.value.email,
                    phone = binding.etPhone.text.toString(),
                    occupation = binding.etOccupation.text.toString()
                )
                if (hasTriedToSave) renderValidationErrors()
            }
        }
        binding.etFullName.addTextChangedListener(onTextChanged = textWatcher)
        binding.etPhone.addTextChangedListener(onTextChanged = textWatcher)
        binding.etOccupation.addTextChangedListener(onTextChanged = textWatcher)

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
        updateTextIfChanged(binding.etOccupation, state.occupation)
        renderAvatar(state.avatarUri)
        renderEditMode(state)
        isRendering = false
        binding.btnSaveProfile.isEnabled = state.isEditing && !state.isSaving && !state.isLoading
        binding.btnSaveProfile.text = if (state.isSaving) {
            getString(R.string.profile_saving)
        } else {
            getString(R.string.profile_save)
        }
        if (hasTriedToSave) renderValidationErrors()

        state.errorMessage?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            viewModel.consumeSaveResult()
        }
        if (state.isSaved) {
            Toast.makeText(
                requireContext(),
                state.successMessage ?: getString(R.string.profile_save_success),
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

    private fun openAvatarPickerIfEditing() {
        if (viewModel.uiState.value.isEditing) {
            openAvatarPicker()
        } else {
            Toast.makeText(requireContext(), getString(R.string.profile_edit_first), Toast.LENGTH_SHORT).show()
        }
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
        val phone = binding.etPhone.text.toString().trim()

        val isPhoneValid = phone.isBlank() || PHONE_REGEX.matches(phone)

        binding.etFullName.error = if (fullName.isBlank()) getString(R.string.profile_full_name_required) else null
        binding.etEmail.error = null
        binding.etPhone.error = if (isPhoneValid) null else getString(R.string.profile_phone_invalid)

        return fullName.isNotBlank() && isPhoneValid
    }

    private fun renderEditMode(state: ProfileUiState) {
        val enabled = state.isEditing && !state.isSaving && !state.isLoading
        setInputEnabled(binding.etFullName, enabled)
        setInputEnabled(binding.etEmail, false)
        setInputEnabled(binding.etPhone, enabled)
        setInputEnabled(binding.etOccupation, enabled)
        binding.btnEditAvatar.visibility = View.VISIBLE
        binding.btnEditAvatar.alpha = if (enabled) 1f else 0.48f
        binding.btnEditProfile.visibility = if (state.isEditing) View.GONE else View.VISIBLE
        binding.btnSaveProfile.visibility = if (state.isEditing) View.VISIBLE else View.GONE
    }

    private fun setInputEnabled(editText: android.widget.EditText, enabled: Boolean) {
        editText.isFocusable = enabled
        editText.isFocusableInTouchMode = enabled
        editText.isCursorVisible = enabled
        editText.isLongClickable = enabled
        editText.alpha = 1f
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
    }
}

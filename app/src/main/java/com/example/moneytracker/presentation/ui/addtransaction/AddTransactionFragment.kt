package com.example.moneytracker.presentation.ui.addtransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.databinding.FragmentAddTransactionBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.presentation.uistate.AddTransactionUiState
import com.example.moneytracker.presentation.viewmodel.AddTransactionViewModel
import kotlinx.coroutines.launch

class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTransactionViewModel by viewModels {
        AddTransactionViewModel.Factory(AppContainer.addTransactionUseCase)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        binding.etName.addTextChangedListener { viewModel.onNameChanged(it.toString()) }
        binding.etAmount.addTextChangedListener { viewModel.onAmountChanged(it.toString()) }
        binding.etDate.addTextChangedListener { viewModel.onDateChanged(it.toString()) }
        binding.btnSaveTransaction.setOnClickListener { viewModel.saveTransaction() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun setupSpinners() {
        binding.spType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf(TYPE_PLACEHOLDER) + TransactionType.entries.map { it.name }
        )
        binding.spCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf(CATEGORY_PLACEHOLDER) + TransactionCategory.entries.map { it.name }
        )

        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    viewModel.onTypeCleared()
                } else {
                    viewModel.onTypeSelected(TransactionType.entries[position - 1])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    viewModel.onCategoryCleared()
                } else {
                    viewModel.onCategorySelected(TransactionCategory.entries[position - 1])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun renderState(state: AddTransactionUiState) {
        binding.btnSaveTransaction.isEnabled = !state.isSaving

        if (binding.etDate.text.toString() != state.date) {
            binding.etDate.setText(state.date)
        }

        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        if (state.isSaved) {
            Toast.makeText(context, "Da luu giao dich", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val TYPE_PLACEHOLDER = "Select type"
        const val CATEGORY_PLACEHOLDER = "Select category"
    }
}

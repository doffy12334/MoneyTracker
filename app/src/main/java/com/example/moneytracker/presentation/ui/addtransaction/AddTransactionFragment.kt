package com.example.moneytracker.presentation.ui.addtransaction

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.example.moneytracker.databinding.FragmentAddTransactionBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.presentation.uistate.AddTransactionUiState
import com.example.moneytracker.presentation.util.BudgetNotificationHelper
import com.example.moneytracker.presentation.util.NotificationStateTracker
import com.example.moneytracker.presentation.viewmodel.AddTransactionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private var hasHandledSavedState = false
    private var pendingBudgetAlert: BudgetAlert? = null
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingBudgetAlert?.show()
        }
        pendingBudgetAlert = null
        findNavController().navigateUp()
    }
    private val viewModel: AddTransactionViewModel by viewModels {
        AddTransactionViewModel.Factory(
            AppContainer.addTransactionUseCase,
            AppContainer.getSettingsUseCase
        )
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
        updateAmountHint()
        
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.etName.addTextChangedListener { viewModel.onNameChanged(it.toString()) }
        binding.etAmount.addTextChangedListener { viewModel.onAmountChanged(it.toString()) }
        binding.etCustomCategory.addTextChangedListener { viewModel.onCustomCategoryChanged(it.toString()) }
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.btnSaveTransaction.setOnClickListener {
            hasHandledSavedState = false
            viewModel.saveTransaction()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAmountHint()
    }

    private fun updateAmountHint() {
        binding.etAmount.hint =
            "${getString(R.string.add_transaction_amount_hint)} (${AppContainer.getSettingsUseCase().currency.code})"
    }

    private fun showDatePicker() {
        val initialDate = parseSelectedDate() ?: Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                viewModel.onDateChanged(selectedDate.toInputDate())
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun parseSelectedDate(): Calendar? {
        val value = binding.etDate.text.toString().trim()
        if (value.isBlank()) return null
        return runCatching {
            Calendar.getInstance().apply {
                time = SimpleDateFormat(INPUT_DATE_PATTERN, currentLocale()).parse(value)
                    ?: return null
            }
        }.getOrNull()
    }

    private fun Calendar.toInputDate(): String {
        return SimpleDateFormat(INPUT_DATE_PATTERN, currentLocale()).format(time)
    }

    private fun currentLocale(): Locale {
        return resources.configuration.locales[0] ?: Locale.getDefault()
    }

    private fun setupSpinners() {
        binding.spType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.add_transaction_type_placeholder)) + TransactionType.entries.map { getLocalizedTypeName(it) }
        )
        binding.spCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.add_transaction_category_placeholder)) + TransactionCategory.entries.map { getLocalizedCategoryName(it) }
        )

        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    viewModel.onTypeCleared()
                } else {
                    viewModel.onTypeSelected(TransactionType.entries[position - 1])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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

        binding.etCustomCategory.visibility = if (state.selectedCategory == TransactionCategory.OTHER) View.VISIBLE else View.GONE
        if (binding.etCustomCategory.text.toString() != state.customCategory) {
            binding.etCustomCategory.setText(state.customCategory)
        }

        state.errorMessage?.let {
            Toast.makeText(context, getString(it), Toast.LENGTH_SHORT).show()
        }

        if (state.isSaved && !hasHandledSavedState) {
            hasHandledSavedState = true
            handleSavedTransaction(state)
        }
    }

    private fun handleSavedTransaction(state: AddTransactionUiState) {
        Toast.makeText(context, getString(R.string.add_transaction_saved), Toast.LENGTH_SHORT)
            .show()
        if (state.selectedType != TransactionType.EXPENSE || state.selectedCategory == null) {
            findNavController().navigateUp()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val budgetAlert = findExceededBudgetAlert(state.selectedCategory)
            if (budgetAlert == null) {
                findNavController().navigateUp()
                return@launch
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                pendingBudgetAlert = budgetAlert
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@launch
            }

            budgetAlert.show()
            findNavController().navigateUp()
        }
    }

    private suspend fun findExceededBudgetAlert(category: TransactionCategory): BudgetAlert? {
        val settings = AppContainer.getSettingsUseCase()
        if (!settings.notificationsEnabled) return null
        val categoryProgress = AppContainer.getBudgetOverviewUseCase()
            .categoryProgress
            .firstOrNull { it.category == category && it.progressPercent >= BUDGET_WARNING_THRESHOLD }
            ?: return null
        val threshold = if (categoryProgress.progressPercent >= BUDGET_EXCEEDED_THRESHOLD) {
            BUDGET_EXCEEDED_THRESHOLD
        } else {
            BUDGET_WARNING_THRESHOLD
        }
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        if (!NotificationStateTracker(requireContext()).shouldNotifyBudgetThreshold(
                category = categoryProgress.category,
                monthKey = monthKey,
                threshold = threshold
            )
        ) {
            return null
        }
        return BudgetAlert(
            category = categoryProgress.category,
            spentAmount = categoryProgress.spentAmount,
            limitAmount = categoryProgress.limitAmount,
            threshold = threshold
        )
    }

    private fun BudgetAlert.show() {
        BudgetNotificationHelper(requireContext()).showBudgetExceeded(
            category = category,
            spentAmount = spentAmount,
            limitAmount = limitAmount,
            currency = AppContainer.getSettingsUseCase().currency,
            threshold = threshold
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val INPUT_DATE_PATTERN = "dd/MM/yyyy"
        const val BUDGET_WARNING_THRESHOLD = 80
        const val BUDGET_EXCEEDED_THRESHOLD = 100
    }

    private data class BudgetAlert(
        val category: TransactionCategory,
        val spentAmount: Double,
        val limitAmount: Double,
        val threshold: Int
    )

    private fun getLocalizedTypeName(type: TransactionType): String {
        return when (type) {
            TransactionType.INCOME -> getString(R.string.income)
            TransactionType.EXPENSE -> getString(R.string.expense)
        }
    }

    private fun getLocalizedCategoryName(category: TransactionCategory): String {
        return when (category) {
            TransactionCategory.FOOD -> getString(R.string.category_food)
            TransactionCategory.TRANSPORT -> getString(R.string.category_transport)
            TransactionCategory.SHOPPING -> getString(R.string.category_shopping)
            TransactionCategory.SALARY -> getString(R.string.category_salary)
            TransactionCategory.ENTERTAINMENT -> getString(R.string.category_entertainment)
            TransactionCategory.OTHER -> getString(R.string.category_other)
        }
    }
}

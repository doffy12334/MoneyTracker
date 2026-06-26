package com.example.moneytracker.presentation.ui.budget

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneytracker.R
import com.example.moneytracker.databinding.DialogBudgetLimitBinding
import com.example.moneytracker.databinding.DialogDepositSavingBinding
import com.example.moneytracker.databinding.DialogSavingGoalBinding
import com.example.moneytracker.databinding.FragmentBudgetBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.settings.AppCurrency
import com.example.moneytracker.domain.model.budget.BudgetCategoryProgress
import com.example.moneytracker.domain.model.budget.BudgetOverview
import com.example.moneytracker.domain.model.budget.SavingGoalProgress
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.presentation.adapter.BudgetLimitAdapter
import com.example.moneytracker.presentation.adapter.SavingGoalAdapter
import com.example.moneytracker.presentation.uistate.BudgetUiState
import com.example.moneytracker.presentation.util.CurrencyFormatter
import com.example.moneytracker.presentation.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels {
        BudgetViewModel.Factory(
            AppContainer.getBudgetOverviewUseCase,
            AppContainer.saveBudgetLimitUseCase,
            AppContainer.deleteBudgetLimitUseCase,
            AppContainer.saveSavingGoalUseCase,
            AppContainer.deleteSavingGoalUseCase
        )
    }

    private lateinit var budgetAdapter: BudgetLimitAdapter
    private lateinit var savingGoalAdapter: SavingGoalAdapter
    private var appCurrency = AppCurrency.VND

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        binding.btnAddBudget.setOnClickListener { showBudgetDialog() }
        binding.btnAddSavingGoal.setOnClickListener { showSavingGoalDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::renderState) }
                launch {
                    viewModel.messages.collect {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyCurrencySettings()
        viewModel.loadBudget()
    }

    private fun setupAdapters() {
        budgetAdapter = BudgetLimitAdapter(
            onEdit = { showBudgetDialog(it) },
            onDelete = { confirmDeleteBudget(it) }
        )
        savingGoalAdapter = SavingGoalAdapter(
            onEdit = { showSavingGoalDialog(it) },
            onDeposit = { showDepositDialog(it) },
            onWithdraw = { showWithdrawDialog(it) },
            onDelete = { confirmDeleteSavingGoal(it) }
        )

        binding.rvBudgets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.layout_animation_fade_slide_from_right
            )
        }
        binding.rvSavingGoals.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = savingGoalAdapter
        }
    }

    private fun applyCurrencySettings() {
        val currency = AppContainer.getSettingsUseCase().currency
        if (appCurrency == currency) return
        appCurrency = currency
        if (::budgetAdapter.isInitialized) budgetAdapter.setCurrency(currency)
        if (::savingGoalAdapter.isInitialized) savingGoalAdapter.setCurrency(currency)
    }

    private fun renderState(state: BudgetUiState) {
        when (state) {
            BudgetUiState.Loading -> renderOverview(
                BudgetOverview(
                    0.0,
                    0.0,
                    emptyList(),
                    emptyList()))

            is BudgetUiState.Success -> renderOverview(state.overview)
            is BudgetUiState.Error -> {
                renderOverview(BudgetOverview(0.0, 0.0, emptyList(), emptyList()))
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderOverview(overview: BudgetOverview) {
        val totalPercent = if (overview.totalBudget > 0.0) {
            ((overview.totalSpent / overview.totalBudget) * 100).toInt()
        } else {
            0
        }
        binding.tvBudgetPeriod.text = getString(R.string.budget_monthly_period, currentMonthLabel())
        binding.tvTotalBudgetValue.text = formatMoney(overview.totalBudget)
        binding.pbTotal.progress = totalPercent.coerceIn(0, 100)
        binding.tvTotalSpent.text = getString(
            R.string.budget_total_spent,
            formatMoney(overview.totalSpent),
            totalPercent.coerceAtLeast(0)
        )

        binding.tvEmptyBudgets.isVisible = overview.categoryProgress.isEmpty()
        binding.rvBudgets.isVisible = overview.categoryProgress.isNotEmpty()
        budgetAdapter.submitList(overview.categoryProgress)
        binding.rvBudgets.scheduleLayoutAnimation()

        binding.tvEmptyGoals.isVisible = overview.savingGoals.isEmpty()
        binding.rvSavingGoals.isVisible = overview.savingGoals.isNotEmpty()
        savingGoalAdapter.submitList(overview.savingGoals)
    }

    private fun showBudgetDialog(item: BudgetCategoryProgress? = null) {
        val dialogBinding = DialogBudgetLimitBinding.inflate(layoutInflater).apply {
            tvDialogTitle.text = getString(
                if (item == null) R.string.budget_limit_add_title else R.string.budget_limit_edit_title
            )
            spCategory.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                TransactionCategory.entries.map { it.localizedName() }
            )
            spCategory.setSelection(
                TransactionCategory.entries.indexOf(
                    item?.category ?: TransactionCategory.FOOD))
            etAmount.hint = getString(R.string.budget_limit_amount_hint, appCurrency.code)
            etAmount.setText(item?.limitAmount?.takeIf { it > 0.0 }
                ?.let { appCurrency.fromVnd(it).toPlainAmount() }
                .orEmpty())
        }
        val dialog = createThemedDialog(dialogBinding.root)
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category =
                TransactionCategory.entries[dialogBinding.spCategory.selectedItemPosition]
            viewModel.saveBudgetLimit(category, appCurrency.toVnd(amount))
            dialog.dismiss()
        }
        dialog.showThemed()
    }

    private fun showSavingGoalDialog(item: SavingGoalProgress? = null) {
        val dialogBinding = DialogSavingGoalBinding.inflate(layoutInflater).apply {
            tvDialogTitle.text = getString(
                if (item == null) R.string.saving_wallet_add_title else R.string.saving_wallet_edit_title
            )
            etTitle.setText(item?.goal?.title.orEmpty())
            etTargetAmount.hint =
                getString(R.string.saving_wallet_target_amount_hint, appCurrency.code)
            etTargetAmount.setText(item?.goal?.targetAmount?.takeIf { it > 0.0 }
                ?.let { appCurrency.fromVnd(it).toPlainAmount() }
                .orEmpty())
            etCurrentAmount.hint =
                getString(R.string.saving_wallet_saved_amount_hint, appCurrency.code)
            etCurrentAmount.setText(item?.goal?.currentAmount?.takeIf { it > 0.0 }
                ?.let { appCurrency.fromVnd(it).toPlainAmount() }
                .orEmpty())
        }
        val dialog = createThemedDialog(dialogBinding.root)
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            viewModel.saveSavingGoal(
                id = item?.goal?.id,
                title = dialogBinding.etTitle.text.toString(),
                targetAmount = appCurrency.toVnd(
                    dialogBinding.etTargetAmount.text.toString()
                        .toDoubleOrNull() ?: 0.0),
                currentAmount = appCurrency.toVnd(
                    dialogBinding.etCurrentAmount.text.toString()
                        .toDoubleOrNull() ?: 0.0)
            )
            dialog.dismiss()
        }
        dialog.showThemed()
    }

    private fun showDepositDialog(item: SavingGoalProgress) {
        val dialogBinding = DialogDepositSavingBinding.inflate(layoutInflater).apply {
            tvDialogTitle.text = getString(R.string.add_money)
            tvGoalSummary.text = getString(
                R.string.saving_wallet_goal_summary,
                item.goal.title,
                formatMoney(item.goal.currentAmount),
                formatMoney(item.goal.targetAmount)
            )
            etDepositAmount.hint =
                getString(R.string.saving_wallet_deposit_amount_hint, appCurrency.code)
        }
        val dialog = createThemedDialog(dialogBinding.root)
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDeposit.setOnClickListener {
            val depositAmount =
                dialogBinding.etDepositAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (depositAmount <= 0.0) {
                Toast.makeText(
                    requireContext(),
                    R.string.amount_must_be_positive,
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveSavingGoal(
                id = item.goal.id,
                title = item.goal.title,
                targetAmount = item.goal.targetAmount,
                currentAmount = item.goal.currentAmount + appCurrency.toVnd(depositAmount)
            )
            dialog.dismiss()
        }
        dialog.showThemed()
    }

    private fun showWithdrawDialog(item: SavingGoalProgress) {
        val dialogBinding = DialogDepositSavingBinding.inflate(layoutInflater).apply {
            tvDialogTitle.text = getString(R.string.withdraw_money)
            tvGoalSummary.text = getString(
                R.string.saving_wallet_goal_summary,
                item.goal.title,
                formatMoney(item.goal.currentAmount),
                formatMoney(item.goal.targetAmount)
            )
            etDepositAmount.hint =
                getString(R.string.saving_wallet_withdraw_amount_hint, appCurrency.code)
            btnDeposit.text = getString(R.string.withdraw_money)
        }
        val dialog = createThemedDialog(dialogBinding.root)
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDeposit.setOnClickListener {
            val withdrawAmount =
                dialogBinding.etDepositAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (withdrawAmount <= 0.0) {
                Toast.makeText(
                    requireContext(),
                    R.string.amount_must_be_positive,
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (appCurrency.toVnd(withdrawAmount) > item.goal.currentAmount) {
                Toast.makeText(
                    requireContext(),
                    R.string.amount_exceeds_savings,
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveSavingGoal(
                id = item.goal.id,
                title = item.goal.title,
                targetAmount = item.goal.targetAmount,
                currentAmount = item.goal.currentAmount - appCurrency.toVnd(withdrawAmount)
            )
            dialog.dismiss()
        }
        dialog.showThemed()
    }

    private fun confirmDeleteBudget(item: BudgetCategoryProgress) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.budget_limit_delete_title)
            .setMessage(
                getString(
                    R.string.budget_limit_delete_message,
                    item.category.localizedName()))
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteBudgetLimit(item.category) }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun createThemedDialog(contentView: View): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setView(contentView)
            .create()
    }

    private fun AlertDialog.showThemed() {
        show()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun confirmDeleteSavingGoal(item: SavingGoalProgress) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.saving_wallet_delete_title)
            .setMessage(getString(R.string.saving_wallet_delete_message, item.goal.title))
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteSavingGoal(item.goal.id) }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun TransactionCategory.localizedName(): String {
        return getString(
            when (this) {
                TransactionCategory.FOOD -> R.string.category_food
                TransactionCategory.TRANSPORT -> R.string.category_transport
                TransactionCategory.SHOPPING -> R.string.category_shopping
                TransactionCategory.SALARY -> R.string.category_salary
                TransactionCategory.ENTERTAINMENT -> R.string.category_entertainment
                TransactionCategory.OTHER -> R.string.category_other
            }
        )
    }

    private fun currentMonthLabel(): String {
        return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    private fun formatMoney(amountInVnd: Double): String {
        return CurrencyFormatter.formatFromVnd(amountInVnd, appCurrency)
    }

    private fun Double.toPlainAmount(): String {
        return if (this % 1.0 == 0.0) toLong().toString() else toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

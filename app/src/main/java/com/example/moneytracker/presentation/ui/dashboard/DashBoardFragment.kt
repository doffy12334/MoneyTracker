package com.example.moneytracker.presentation.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentDashBoardBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.presentation.adapter.TransactionAdapter
import com.example.moneytracker.presentation.uistate.DashboardUiState
import com.example.moneytracker.presentation.util.CurrencyFormatter
import com.example.moneytracker.presentation.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashBoardFragment : Fragment() {
    private var _binding: FragmentDashBoardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(AppContainer.getDashboardSummaryUseCase)
    }

    private lateinit var transactionAdapter: TransactionAdapter
    private var appCurrency = AppCurrency.VND
    private var currencyFormatter = CurrencyFormatter.create(appCurrency)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionAdapter = TransactionAdapter()
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.layout_animation_fade_slide_from_right
            )
        }

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addTransactionFragment)
        }
        binding.tvSeeAllTransactions.setOnClickListener {
            viewModel.toggleShowAllTransactions()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyCurrencySettings()
        viewModel.loadDashboard()
    }

    private fun applyCurrencySettings() {
        val currency = AppContainer.getSettingsUseCase().currency
        if (appCurrency == currency) return
        appCurrency = currency
        currencyFormatter = CurrencyFormatter.create(currency)
        if (::transactionAdapter.isInitialized) {
            transactionAdapter.setCurrency(currency)
        }
    }

    private fun renderState(state: DashboardUiState) {
        when (state) {
            DashboardUiState.Loading -> {
                transactionAdapter.submitList(emptyList())
                binding.tvTotalBalance.text = currencyFormatter.format(0.0)
                binding.tvIncomeAmount.text = currencyFormatter.format(0.0)
                binding.tvExpenseAmount.text = currencyFormatter.format(0.0)
                binding.tvBalanceRate.text = formatBalanceRate(0.0)
                binding.tvSeeAllTransactions.text = getString(R.string.xem_t_t_c)
            }

            is DashboardUiState.Success -> {
                binding.tvTotalBalance.text = currencyFormatter.format(state.totalBalance)
                binding.tvIncomeAmount.text = currencyFormatter.format(state.totalIncome)
                binding.tvExpenseAmount.text = currencyFormatter.format(state.totalExpense)
                binding.tvBalanceRate.text = formatBalanceRate(state.balanceRate)
                binding.tvSeeAllTransactions.text = if (state.showAllTransactions) {
                    getString(R.string.show_less)
                } else {
                    getString(R.string.xem_t_t_c)
                }
                transactionAdapter.submitList(state.transactions) {
                    binding.rvTransactions.scheduleLayoutAnimation()
                }
            }

            is DashboardUiState.Error -> {
                transactionAdapter.submitList(emptyList())
                binding.tvTotalBalance.text = state.message
                binding.tvIncomeAmount.text = currencyFormatter.format(0.0)
                binding.tvExpenseAmount.text = currencyFormatter.format(0.0)
                binding.tvBalanceRate.text = formatBalanceRate(0.0)
                binding.tvSeeAllTransactions.text = getString(R.string.xem_t_t_c)
            }
        }
    }

    private fun formatBalanceRate(rate: Double): String {
        val formattedRate = String.format(Locale.getDefault(), "%.1f", kotlin.math.abs(rate))
        return if (rate >= 0.0) {
            getString(R.string.balance_rate_saved, formattedRate)
        } else {
            getString(R.string.balance_rate_over_income, formattedRate)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

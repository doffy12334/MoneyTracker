package com.example.moneytracker.presentation.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.moneytracker.presentation.adapter.TransactionAdapter
import com.example.moneytracker.presentation.uistate.DashboardUiState
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
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

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
        }

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addTransactionFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDashboard()
    }

    private fun renderState(state: DashboardUiState) {
        when (state) {
            DashboardUiState.Loading -> {
                transactionAdapter.submitList(emptyList())
                binding.tvTotalBalance.text = currencyFormatter.format(0.0)
                binding.tvIncomeAmount.text = currencyFormatter.format(0.0)
                binding.tvExpenseAmount.text = currencyFormatter.format(0.0)
            }

            is DashboardUiState.Success -> {
                binding.tvTotalBalance.text = currencyFormatter.format(state.totalBalance)
                binding.tvIncomeAmount.text = currencyFormatter.format(state.totalIncome)
                binding.tvExpenseAmount.text = currencyFormatter.format(state.totalExpense)
                transactionAdapter.submitList(state.transactions)
            }

            is DashboardUiState.Error -> {
                transactionAdapter.submitList(emptyList())
                binding.tvTotalBalance.text = state.message
                binding.tvIncomeAmount.text = currencyFormatter.format(0.0)
                binding.tvExpenseAmount.text = currencyFormatter.format(0.0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

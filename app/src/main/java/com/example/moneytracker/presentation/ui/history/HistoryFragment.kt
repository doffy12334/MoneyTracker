package com.example.moneytracker.presentation.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentHistoryBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.adapter.TransactionAdapter
import com.example.moneytracker.presentation.uistate.HistoryUiState
import com.example.moneytracker.presentation.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(AppContainer.getTransactionsUseCase)
    }
    private val transactionAdapter = TransactionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.layout_animation_fade_slide_from_right
            )
        }
        binding.etSearch.addTextChangedListener {
            viewModel.onSearchQueryChanged(it.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: HistoryUiState) {
        transactionAdapter.submitList(state.transactions) {
            binding.rvHistory.scheduleLayoutAnimation()
        }
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTransactions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

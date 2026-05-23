package com.example.moneytracker.presentation.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentExportReportBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.ExportFileFormat
import com.example.moneytracker.domain.model.ExportPeriod
import com.example.moneytracker.presentation.uistate.ExportReportUiState
import com.example.moneytracker.presentation.viewmodel.ExportReportViewModel
import kotlinx.coroutines.launch

class ExportReportFragment : Fragment() {
    private var _binding: FragmentExportReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExportReportViewModel by viewModels {
        ExportReportViewModel.Factory(AppContainer.exportReportUseCase)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.optionCurrentMonth.setOnClickListener {
            viewModel.selectPeriod(ExportPeriod.CURRENT_MONTH)
        }
        binding.optionLastMonth.setOnClickListener {
            viewModel.selectPeriod(ExportPeriod.LAST_MONTH)
        }
        binding.optionCustom.setOnClickListener {
            viewModel.selectPeriod(ExportPeriod.CUSTOM)
        }
        binding.optionCsv.setOnClickListener {
            viewModel.selectFormat(ExportFileFormat.CSV)
        }
        binding.optionPdf.setOnClickListener {
            viewModel.selectFormat(ExportFileFormat.PDF)
        }
        binding.btnExportReport.setOnClickListener {
            viewModel.exportReport()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: ExportReportUiState) {
        setSelected(binding.optionCurrentMonth, state.selectedPeriod == ExportPeriod.CURRENT_MONTH)
        setSelected(binding.optionLastMonth, state.selectedPeriod == ExportPeriod.LAST_MONTH)
        setSelected(binding.optionCustom, state.selectedPeriod == ExportPeriod.CUSTOM)
        setSelected(binding.optionCsv, state.selectedFormat == ExportFileFormat.CSV)
        setSelected(binding.optionPdf, state.selectedFormat == ExportFileFormat.PDF)
        binding.btnExportReport.isEnabled = !state.isExporting

        val message = state.errorMessage ?: state.successMessage
        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    private fun setSelected(view: TextView, selected: Boolean) {
        view.setBackgroundResource(if (selected) R.drawable.bg_input_field else R.drawable.bg_soft_green_card)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

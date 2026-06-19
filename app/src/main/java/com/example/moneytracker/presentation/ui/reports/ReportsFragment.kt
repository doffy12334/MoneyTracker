package com.example.moneytracker.presentation.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentReportsBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.domain.model.ReportCategoryBreakdown
import com.example.moneytracker.domain.model.ReportPeriod
import com.example.moneytracker.presentation.adapter.ReportCategoryAdapter
import com.example.moneytracker.presentation.uistate.ReportsUiState
import com.example.moneytracker.presentation.util.CurrencyFormatter
import com.example.moneytracker.presentation.viewmodel.ReportsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import java.text.NumberFormat

class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by viewModels {
        ReportsViewModel.Factory(AppContainer.getSpendingReportUseCase)
    }
    private val categoryAdapter = ReportCategoryAdapter()
    private var appCurrency = AppCurrency.VND
    private var currencyFormatter: NumberFormat = CurrencyFormatter.create(appCurrency)
    private var lastChartSignature: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        binding.rvBreakdown.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.layout_animation_fade_slide_from_right
            )
        }

        binding.tvFilterWeek.setOnClickListener { viewModel.onPeriodSelected(ReportPeriod.WEEKLY) }
        binding.tvFilterMonth.setOnClickListener { viewModel.onPeriodSelected(ReportPeriod.MONTHLY) }
        binding.tvFilterYear.setOnClickListener { viewModel.onPeriodSelected(ReportPeriod.YEARLY) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyCurrencySettings()
        viewModel.loadReport()
    }

    private fun applyCurrencySettings() {
        val currency = AppContainer.getSettingsUseCase().currency
        if (appCurrency == currency) return
        appCurrency = currency
        currencyFormatter = CurrencyFormatter.create(currency)
        categoryAdapter.setCurrency(currency)
        lastChartSignature = ""
    }

    private fun setupChart() {
        binding.spendingPieChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            setTouchEnabled(true)
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 68f
            transparentCircleRadius = 74f
            setTransparentCircleColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
            setTransparentCircleAlpha(40)
            setNoDataText(getString(R.string.reports_no_spending))
            setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun renderState(state: ReportsUiState) {
        renderPeriodTabs(state.selectedPeriod)
        renderChart(state.totalSpent, state.breakdown)
        categoryAdapter.submitList(state.breakdown)
        binding.rvBreakdown.scheduleLayoutAnimation()

        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderPeriodTabs(selectedPeriod: ReportPeriod) {
        binding.tvFilterWeek.renderPeriodTab(selectedPeriod == ReportPeriod.WEEKLY)
        binding.tvFilterMonth.renderPeriodTab(selectedPeriod == ReportPeriod.MONTHLY)
        binding.tvFilterYear.renderPeriodTab(selectedPeriod == ReportPeriod.YEARLY)
    }

    private fun TextView.renderPeriodTab(isSelected: Boolean) {
        if (isSelected) {
            setBackgroundResource(R.drawable.bg_filter_active)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.inverse_text))
        } else {
            setBackgroundResource(R.drawable.bg_filter_inactive)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun renderChart(totalSpent: Double, breakdown: List<ReportCategoryBreakdown>) {
        val chartSignature = buildString {
            append(appCurrency.code)
            append(':')
            append(totalSpent)
            breakdown.forEach { item ->
                append('|')
                append(item.categoryName)
                append(':')
                append(item.amount)
            }
        }
        if (chartSignature == lastChartSignature) return
        lastChartSignature = chartSignature

        val chart = binding.spendingPieChart
        chart.animate()
            .alpha(0.72f)
            .scaleX(0.96f)
            .scaleY(0.96f)
            .setDuration(120L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                updateChartData(totalSpent, breakdown)
                chart.rotationAngle = -90f
                chart.animateY(900, Easing.EaseOutQuart)
                chart.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(260L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun updateChartData(totalSpent: Double, breakdown: List<ReportCategoryBreakdown>) {
        val chart = binding.spendingPieChart
        if (breakdown.isEmpty() || totalSpent <= 0.0) {
            renderEmptyChart(chart)
            chart.invalidate()
            return
        }

        val entries = breakdown.map { item ->
            PieEntry(item.displaySliceValue(totalSpent).toFloat(), item.categoryName)
        }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ReportCategoryAdapter.colors
            sliceSpace = 4f
            selectionShift = 8f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            valueTextSize = 11f
        }
        chart.data = PieData(dataSet).apply {
            setDrawValues(false)
        }
        chart.isHighlightPerTapEnabled = true
        chart.centerText = "${getString(R.string.reports_total)}\n${currencyFormatter.format(totalSpent)}"
        chart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        chart.setCenterTextSize(14f)
        chart.invalidate()
    }

    private fun ReportCategoryBreakdown.displaySliceValue(totalSpent: Double): Double {
        if (totalSpent <= 0.0 || amount <= 0.0) return amount
        if (amount / totalSpent >= MIN_VISIBLE_SLICE_RATIO) return amount
        return totalSpent * MIN_VISIBLE_SLICE_RATIO
    }

    private fun renderEmptyChart(chart: com.github.mikephil.charting.charts.PieChart) {
        val entries = ReportCategoryAdapter.colors.map { PieEntry(1f, "") }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ReportCategoryAdapter.colors.map { color -> Color.argb(92, Color.red(color), Color.green(color), Color.blue(color)) }
            sliceSpace = 3f
            selectionShift = 0f
        }
        chart.data = PieData(dataSet).apply {
            setDrawValues(false)
        }
        chart.centerText = getString(R.string.reports_no_spending)
        chart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        chart.setCenterTextSize(13f)
        chart.isHighlightPerTapEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val MIN_VISIBLE_SLICE_RATIO = 0.04
    }
}

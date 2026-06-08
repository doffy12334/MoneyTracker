package com.example.moneytracker.presentation.ui.history

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentHistoryBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.presentation.adapter.TransactionAdapter
import com.example.moneytracker.presentation.uistate.HistoryDateFilter
import com.example.moneytracker.presentation.uistate.HistoryTypeFilter
import com.example.moneytracker.presentation.uistate.HistoryUiState
import com.example.moneytracker.presentation.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(
            AppContainer.getTransactionsUseCase,
            AppContainer.deleteTransactionUseCase
        )
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
        binding.tvWeekly.setOnClickListener { viewModel.onDateFilterChanged(HistoryDateFilter.WEEKLY) }
        binding.tvMonthly.setOnClickListener { viewModel.onDateFilterChanged(HistoryDateFilter.MONTHLY) }
        binding.tvYearly.setOnClickListener { viewModel.onDateFilterChanged(HistoryDateFilter.YEARLY) }
        binding.ivFilter.setOnClickListener { showTypeFilterDialog(viewModel.uiState.value.typeFilter) }
        binding.filterCardview.setOnClickListener { showTypeFilterDialog(viewModel.uiState.value.typeFilter) }
        attachSwipeToDelete()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: HistoryUiState) {
        renderDateFilter(state.dateFilter)
        transactionAdapter.submitList(state.transactions) {
            binding.rvHistory.scheduleLayoutAnimation()
        }
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    private fun renderDateFilter(activeFilter: HistoryDateFilter) {
        binding.tvWeekly.renderFilterChip(activeFilter == HistoryDateFilter.WEEKLY)
        binding.tvMonthly.renderFilterChip(activeFilter == HistoryDateFilter.MONTHLY)
        binding.tvYearly.renderFilterChip(activeFilter == HistoryDateFilter.YEARLY)
        binding.tvHistoryPeriodTitle.text = when (activeFilter) {
            HistoryDateFilter.WEEKLY -> getString(R.string.history_period_this_week)
            HistoryDateFilter.MONTHLY -> getString(R.string.history_period_this_month)
            HistoryDateFilter.YEARLY -> getString(R.string.history_period_this_year)
        }
    }

    private fun TextView.renderFilterChip(isActive: Boolean) {
        setBackgroundResource(if (isActive) R.drawable.bg_filter_active else R.drawable.bg_filter_inactive)
        val textColor = if (isActive) R.color.inverse_text else R.color.text_primary
        setTextColor(ContextCompat.getColor(requireContext(), textColor))
    }

    private fun showTypeFilterDialog(currentFilter: HistoryTypeFilter) {
        val filters = arrayOf(
            HistoryTypeFilter.ALL,
            HistoryTypeFilter.INCOME,
            HistoryTypeFilter.EXPENSE
        )
        val labels = arrayOf(
            getString(R.string.history_type_all),
            getString(R.string.history_type_income),
            getString(R.string.history_type_expense)
        )
        val checkedItem = filters.indexOf(currentFilter)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.history_type_filter_title)
            .setSingleChoiceItems(labels, checkedItem) { dialog, which ->
                viewModel.onTypeFilterChanged(filters[which])
                dialog.dismiss()
            }
            .show()
    }

    private fun attachSwipeToDelete() {
        val deletePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(requireContext(), R.color.error_color)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(requireContext(), android.R.color.white)
            textAlign = Paint.Align.CENTER
            textSize = resources.getDimensionPixelSize(R.dimen.swipe_delete_text_size).toFloat()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash)
        deleteIcon?.setTint(ContextCompat.getColor(requireContext(), android.R.color.white))
        val cornerRadius = resources.getDimensionPixelSize(R.dimen.swipe_delete_corner_radius).toFloat()
        val actionWidth = resources.getDimensionPixelSize(R.dimen.swipe_delete_action_width)
        val iconSize = resources.getDimensionPixelSize(R.dimen.swipe_delete_icon_size)
        val contentGap = resources.getDimensionPixelSize(R.dimen.swipe_delete_content_gap)

        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                viewModel.deleteTransaction(transactionAdapter.currentList[position])
                binding.rvHistory.post { transactionAdapter.notifyItemChanged(position) }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val icon = deleteIcon
                val swipeWidth = minOf(kotlin.math.abs(dX).toInt(), actionWidth)
                if (swipeWidth > 0) {
                    val actionRect = if (dX > 0) {
                        RectF(
                            itemView.left.toFloat(),
                            itemView.top.toFloat(),
                            (itemView.left + swipeWidth).toFloat(),
                            itemView.bottom.toFloat()
                        )
                    } else {
                        RectF(
                            (itemView.right - swipeWidth).toFloat(),
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat()
                        )
                    }
                    c.drawRoundRect(actionRect, cornerRadius, cornerRadius, deletePaint)

                    val centerX = actionRect.centerX()
                    val iconTop = (actionRect.centerY() - iconSize - contentGap / 2f).toInt()
                    icon?.setBounds(
                        (centerX - iconSize / 2f).toInt(),
                        iconTop,
                        (centerX + iconSize / 2f).toInt(),
                        iconTop + iconSize
                    )
                    icon?.draw(c)

                    val textBaseline = actionRect.centerY() + iconSize / 2f + contentGap - (labelPaint.descent() + labelPaint.ascent()) / 2f
                    c.drawText(getString(R.string.action_delete), centerX, textBaseline, labelPaint)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.rvHistory)
    }

    override fun onResume() {
        super.onResume()
        transactionAdapter.setCurrency(AppContainer.getSettingsUseCase().currency)
        viewModel.loadTransactions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

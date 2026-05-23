package com.example.moneytracker.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytracker.databinding.ItemReportCategoryBinding
import com.example.moneytracker.domain.model.ReportCategoryBreakdown
import java.text.NumberFormat
import java.util.Locale

class ReportCategoryAdapter : RecyclerView.Adapter<ReportCategoryAdapter.ReportCategoryViewHolder>() {
    private val items = mutableListOf<ReportCategoryBreakdown>()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    fun submitList(newItems: List<ReportCategoryBreakdown>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportCategoryViewHolder {
        val binding = ItemReportCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportCategoryViewHolder, position: Int) {
        holder.bind(items[position], colors[position % colors.size])
    }

    override fun getItemCount(): Int = items.size

    inner class ReportCategoryViewHolder(
        private val binding: ItemReportCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReportCategoryBreakdown, color: Int) {
            binding.viewColorDot.background.setTint(color)
            binding.tvCategoryName.text = item.categoryName
            binding.tvCategoryAmount.text = "${currencyFormatter.format(item.amount)} (${item.percent}%)"
        }
    }

    companion object {
        val colors = listOf(
            Color.parseColor("#6BCB77"),
            Color.parseColor("#4D96FF"),
            Color.parseColor("#A78BFA"),
            Color.parseColor("#FFB703"),
            Color.parseColor("#FFD166"),
            Color.parseColor("#FF6B6B")
        )
    }
}

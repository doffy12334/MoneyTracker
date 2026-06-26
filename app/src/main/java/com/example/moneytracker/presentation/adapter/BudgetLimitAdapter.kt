package com.example.moneytracker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytracker.R
import com.example.moneytracker.databinding.ItemBudgetBinding
import com.example.moneytracker.domain.model.settings.AppCurrency
import com.example.moneytracker.domain.model.budget.BudgetCategoryProgress
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.presentation.util.CurrencyFormatter

class BudgetLimitAdapter(
    private val onEdit: (BudgetCategoryProgress) -> Unit,
    private val onDelete: (BudgetCategoryProgress) -> Unit
) : RecyclerView.Adapter<BudgetLimitAdapter.BudgetLimitViewHolder>() {
    private val items = mutableListOf<BudgetCategoryProgress>()
    private var currency = AppCurrency.VND

    fun setCurrency(currency: AppCurrency) {
        if (this.currency == currency) return
        this.currency = currency
        notifyDataSetChanged()
    }

    fun submitList(newItems: List<BudgetCategoryProgress>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetLimitViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetLimitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetLimitViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BudgetLimitViewHolder(
        private val binding: ItemBudgetBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BudgetCategoryProgress) {
            val context = binding.root.context
            binding.ivCatIcon.setImageResource(item.category.iconRes())
            binding.tvBudgetName.text = item.category.localizedName(context)
            binding.tvBudgetLimit.text = context.getString(
                R.string.budget_limit_label,
                CurrencyFormatter.formatFromVnd(item.limitAmount, currency)
            )
            binding.tvBudgetPercent.text = "${item.progressPercent}%"
            binding.tvBudgetPercent.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.isExceeded) R.color.error_color else R.color.info_color
                )
            )
            binding.pbBudget.progress = item.progressPercent.coerceIn(0, 100)
            binding.tvSpentInfo.text = if (item.isExceeded) {
                context.getString(
                    R.string.budget_spent_over,
                    CurrencyFormatter.formatFromVnd(item.spentAmount, currency),
                    CurrencyFormatter.formatFromVnd(item.spentAmount - item.limitAmount, currency)
                )
            } else {
                context.getString(
                    R.string.budget_spent_left,
                    CurrencyFormatter.formatFromVnd(item.spentAmount, currency),
                    CurrencyFormatter.formatFromVnd(item.remainingAmount, currency)
                )
            }
            binding.root.setOnClickListener { onEdit(item) }
            binding.root.setOnLongClickListener {
                onDelete(item)
                true
            }
            binding.btnDeleteBudget.setOnClickListener { onDelete(item) }
        }
    }

    private fun TransactionCategory.iconRes(): Int {
        return when (this) {
            TransactionCategory.FOOD -> R.drawable.ic_food
            TransactionCategory.TRANSPORT -> R.drawable.ic_car
            TransactionCategory.SHOPPING -> R.drawable.ic_cart
            TransactionCategory.SALARY -> R.drawable.ic_money_bag
            TransactionCategory.ENTERTAINMENT -> R.drawable.ic_wallet
            TransactionCategory.OTHER -> R.drawable.ic_wallet
        }
    }

    private fun TransactionCategory.localizedName(context: android.content.Context): String {
        return context.getString(
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
}

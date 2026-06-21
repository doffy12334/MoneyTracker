package com.example.moneytracker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytracker.R
import com.example.moneytracker.databinding.ItemSavingGoalBinding
import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.domain.model.SavingGoalProgress
import com.example.moneytracker.presentation.util.CurrencyFormatter

class SavingGoalAdapter(
    private val onEdit: (SavingGoalProgress) -> Unit,
    private val onDeposit: (SavingGoalProgress) -> Unit,
    private val onWithdraw: (SavingGoalProgress) -> Unit,
    private val onDelete: (SavingGoalProgress) -> Unit
) : RecyclerView.Adapter<SavingGoalAdapter.SavingGoalViewHolder>() {
    private val items = mutableListOf<SavingGoalProgress>()
    private var currency = AppCurrency.VND

    fun setCurrency(currency: AppCurrency) {
        if (this.currency == currency) return
        this.currency = currency
        notifyDataSetChanged()
    }

    fun submitList(newItems: List<SavingGoalProgress>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingGoalViewHolder {
        val binding = ItemSavingGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavingGoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavingGoalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class SavingGoalViewHolder(
        private val binding: ItemSavingGoalBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SavingGoalProgress) {
            val context = binding.root.context
            binding.tvGoalName.text = item.goal.title
            binding.tvGoalTarget.text = context.getString(
                R.string.saving_wallet_target_label,
                CurrencyFormatter.formatFromVnd(item.goal.targetAmount, currency)
            )
            binding.tvGoalPercent.text = "${item.progressPercent}%"
            binding.tvGoalPercent.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.isCompleted) R.color.success_color else R.color.info_color
                )
            )
            binding.pbGoal.progress = item.progressPercent.coerceIn(0, 100)
            binding.tvGoalSavedInfo.text = if (item.isCompleted) {
                context.getString(
                    R.string.saving_wallet_completed,
                    CurrencyFormatter.formatFromVnd(item.goal.currentAmount, currency)
                )
            } else {
                context.getString(
                    R.string.saving_wallet_saved_left,
                    CurrencyFormatter.formatFromVnd(item.goal.currentAmount, currency),
                    CurrencyFormatter.formatFromVnd(item.remainingAmount, currency)
                )
            }
            binding.root.setOnClickListener { onEdit(item) }
            binding.root.setOnLongClickListener {
                onDelete(item)
                true
            }
            binding.btnDeleteGoal.setOnClickListener { onDelete(item) }
            binding.btnDepositGoal.setOnClickListener { onDeposit(item) }
            binding.btnWithdrawGoal.setOnClickListener { onWithdraw(item) }
        }
    }
}

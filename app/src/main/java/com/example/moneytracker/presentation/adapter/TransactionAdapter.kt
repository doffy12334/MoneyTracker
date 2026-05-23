package com.example.moneytracker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytracker.R
import com.example.moneytracker.databinding.ItemTransactionBinding
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Transaction) {
            val context = binding.root.context
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            val formattedAmount = formatter.format(item.amount)
            val amountColor = if (item.type == TransactionType.INCOME) {
                R.color.success_color
            } else {
                R.color.error_color
            }

            binding.tvTxName.text = item.name
            binding.tvTxDetail.text = "${item.date} - ${item.category.name}"
            binding.ivTxIcon.setImageResource(item.category.iconRes())
            binding.ivTxIcon.setColorFilter(ContextCompat.getColor(context, amountColor))

            if (item.type == TransactionType.INCOME) {
                binding.tvTxAmount.text = "+$formattedAmount"
            } else {
                binding.tvTxAmount.text = "-$formattedAmount"
            }
            binding.tvTxAmount.setTextColor(ContextCompat.getColor(context, amountColor))
        }

        private fun TransactionCategory.iconRes(): Int {
            return when (this) {
                TransactionCategory.FOOD -> R.drawable.ic_food
                TransactionCategory.TRANSPORT -> R.drawable.ic_car
                TransactionCategory.SHOPPING -> R.drawable.ic_cart
                TransactionCategory.SALARY -> R.drawable.ic_money_bag
                TransactionCategory.ENTERTAINMENT -> R.drawable.ic_diamond_outline
                TransactionCategory.OTHER -> R.drawable.ic_wallet
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem == newItem
    }
}

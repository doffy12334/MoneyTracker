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

            binding.tvTxName.text = item.name
            // Đảm bảo bạn đã thêm thuộc tính category vào model Transaction
            binding.tvTxDetail.text = item.category.name

            // Định dạng tiền tệ theo chuẩn quốc tế
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            val formattedAmount = formatter.format(item.amount)

            if (item.type == TransactionType.INCOME) {
                binding.tvTxAmount.text = "+$formattedAmount"
                binding.tvTxAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green))
            } else {
                binding.tvTxAmount.text = "-$formattedAmount"
                binding.tvTxAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem == newItem
    }
}
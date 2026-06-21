package com.example.moneytracker.domain.model.transaction

enum class TransactionCategory {
    FOOD, TRANSPORT, SHOPPING, SALARY, ENTERTAINMENT, OTHER
}


data class Transaction(
    val id: String,
    val name: String,
    val amount: Double,
    /** Ngày giao dịch. Định dạng chuẩn nên là: yyyy-MM-dd hoặc dd/MM/yyyy. Tuy nhiên do thiết kế ban đầu dùng String, vui lòng giữ nhất quán định dạng khi lưu vào DB. */
    val date: String,
    val category: TransactionCategory,
    val customCategory: String? = null,
    val type: TransactionType,
    val createdAt: String = date
)

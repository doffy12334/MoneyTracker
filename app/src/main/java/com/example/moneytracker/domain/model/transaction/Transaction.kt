package com.example.moneytracker.domain.model.transaction

enum class TransactionCategory {
    FOOD, TRANSPORT, SHOPPING, SALARY, ENTERTAINMENT, OTHER
}


data class Transaction(
    val id: String,
    val name: String,
    val amount: Double,
    val date: String,
    val category: TransactionCategory,
    val type: TransactionType
)
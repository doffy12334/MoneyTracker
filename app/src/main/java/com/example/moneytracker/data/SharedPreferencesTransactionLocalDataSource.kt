package com.example.moneytracker.data

import android.content.Context
import androidx.core.content.edit
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

class SharedPreferencesTransactionLocalDataSource(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : TransactionLocalDataSource {
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override suspend fun getTransactions(): List<Transaction> {
        val rawTransactions = sharedPreferences.getString(currentTransactionsKey(), null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(rawTransactions)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(index) ?: continue
                    add(jsonObject.toTransaction())
                }
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun saveTransactions(transactions: List<Transaction>) {
        sharedPreferences.edit {
            putString(currentTransactionsKey(), transactions.toJsonArray().toString())
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        saveTransactions(listOf(transaction) + getTransactions().filterNot { it.id == transaction.id })
    }

    override suspend fun deleteTransaction(transactionId: String) {
        saveTransactions(getTransactions().filterNot { it.id == transactionId })
    }

    private fun currentTransactionsKey(): String {
        val uid = auth.currentUser?.uid ?: GUEST_USER_ID
        return "$KEY_TRANSACTIONS_PREFIX$uid"
    }

    private fun List<Transaction>.toJsonArray(): JSONArray {
        return JSONArray().apply {
            forEach { transaction ->
                put(
                    JSONObject()
                        .put("id", transaction.id)
                        .put("name", transaction.name)
                        .put("amount", transaction.amount)
                        .put("date", transaction.date)
                        .put("createdAt", transaction.createdAt)
                        .put("category", transaction.category.name)
                        .put("type", transaction.type.name)
                )
            }
        }
    }

    private fun JSONObject.toTransaction(): Transaction {
        return Transaction(
            id = getString("id"),
            name = getString("name"),
            amount = getDouble("amount"),
            date = getString("date"),
            category = enumValueOrDefault(
                value = optString("category"),
                defaultValue = TransactionCategory.OTHER
            ),
            type = enumValueOrDefault(
                value = optString("type"),
                defaultValue = TransactionType.EXPENSE
            ),
            createdAt = optString("createdAt", getString("date"))
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, defaultValue: T): T {
        return runCatching { enumValueOf<T>(value) }.getOrDefault(defaultValue)
    }

    private companion object {
        const val PREF_NAME = "money_tracker_transactions"
        const val KEY_TRANSACTIONS_PREFIX = "transactions_"
        const val GUEST_USER_ID = "guest"
    }
}

package com.example.moneytracker.data

import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseTransactionRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : TransactionRemoteDataSource {

    override suspend fun fetchTransactions(): List<Transaction> {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val formatter = SimpleDateFormat(OUTPUT_DATE_PATTERN, Locale.US)
        return firestore.collection("users")
            .document(uid)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val category = runCatching {
                    TransactionCategory.valueOf(doc.getString("category") ?: TransactionCategory.OTHER.name)
                }.getOrDefault(TransactionCategory.OTHER)
                val type = runCatching {
                    TransactionType.valueOf(doc.getString("type") ?: TransactionType.EXPENSE.name)
                }.getOrDefault(TransactionType.EXPENSE)

                Transaction(
                    id = doc.id,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    amount = doc.getDouble("amount") ?: return@mapNotNull null,
                    date = doc.getTimestamp("date")?.toDate()?.let(formatter::format)
                        ?: doc.getString("date")
                        ?: return@mapNotNull null,
                    category = category,
                    type = type
                )
            }
    }

    override suspend fun pushTransaction(transaction: Transaction) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val transactionDate = parseTransactionDate(transaction.date)
            ?: throw IllegalArgumentException("Ngay giao dich khong hop le. Hay nhap dd/MM/yyyy, dd-MM-yyyy hoac yyyy-MM-dd")
        val data = mapOf(
            "name" to transaction.name,
            "amount" to transaction.amount,
            "date" to Timestamp(transactionDate),
            "createdAt" to FieldValue.serverTimestamp(),
            "category" to transaction.category.name,
            "type" to transaction.type.name
        )
        firestore.collection("users")
            .document(uid)
            .collection("transactions")
            .document(transaction.id)
            .set(data)
            .await()
    }

    private companion object {
        const val OUTPUT_DATE_PATTERN = "dd MMM yyyy"

        val INPUT_DATE_PATTERNS = listOf(
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            OUTPUT_DATE_PATTERN
        )

        fun parseTransactionDate(value: String): Date? {
            val trimmedValue = value.trim()
            return INPUT_DATE_PATTERNS.firstNotNullOfOrNull { pattern ->
                runCatching {
                    SimpleDateFormat(pattern, Locale.US).apply {
                        isLenient = false
                    }.parse(trimmedValue)
                }.getOrNull()
            }
        }
    }
}

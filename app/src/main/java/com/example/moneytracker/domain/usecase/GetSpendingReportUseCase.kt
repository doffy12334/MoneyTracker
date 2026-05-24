package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.ReportCategoryBreakdown
import com.example.moneytracker.domain.model.ReportPeriod
import com.example.moneytracker.domain.model.SpendingReportSummary
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.model.transaction.TransactionType
import com.example.moneytracker.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class GetSpendingReportUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(period: ReportPeriod = ReportPeriod.MONTHLY): SpendingReportSummary {
        val transactions = transactionRepository.getTransactions()
        val anchorDate = transactions.latestReportDate()
        val expenses = transactions
            .filter { it.matchesPeriod(period, anchorDate) }
            .filter { it.type == TransactionType.EXPENSE }

        val totalSpent = expenses.sumOf { it.amount }
        val breakdown = expenses
            .groupBy { it.category }
            .map { (category, transactions) ->
                val amount = transactions.sumOf { it.amount }
                ReportCategoryBreakdown(
                    categoryName = category.displayName(),
                    amount = amount,
                    percent = if (totalSpent > 0.0) ((amount / totalSpent) * 100).roundToInt() else 0
                )
            }
            .sortedByDescending { it.amount }

        return SpendingReportSummary(
            totalSpent = totalSpent,
            breakdown = breakdown
        )
    }

    private fun TransactionCategory.displayName(): String {
        return when (this) {
            TransactionCategory.FOOD -> "Food"
            TransactionCategory.TRANSPORT -> "Transport"
            TransactionCategory.SHOPPING -> "Shopping"
            TransactionCategory.SALARY -> "Salary"
            TransactionCategory.ENTERTAINMENT -> "Entertainment"
            TransactionCategory.OTHER -> "Other"
        }
    }

    private fun List<Transaction>.latestReportDate(): Calendar? {
        val latestDate = mapNotNull { it.parseReportDate() }.maxOrNull() ?: return null
        return Calendar.getInstance().apply { time = latestDate }
    }

    private fun Transaction.matchesPeriod(period: ReportPeriod, anchorDate: Calendar?): Boolean {
        val date = parseReportDate() ?: return false
        val transactionDate = Calendar.getInstance().apply { time = date }
        val anchor = anchorDate ?: Calendar.getInstance()
        return when (period) {
            ReportPeriod.WEEKLY -> transactionDate.isInLastSevenDays(anchor)
            ReportPeriod.MONTHLY ->
                transactionDate.get(Calendar.YEAR) == anchor.get(Calendar.YEAR) &&
                    transactionDate.get(Calendar.MONTH) == anchor.get(Calendar.MONTH)
            ReportPeriod.YEARLY ->
                transactionDate.get(Calendar.YEAR) == anchor.get(Calendar.YEAR)
        }
    }

    private fun Transaction.parseReportDate(): Date? {
        return parseDate(date) ?: parseDate(createdAt).takeIf { createdAt.isNotBlank() }
    }

    private fun parseDate(value: String): Date? {
        val trimmedValue = value.trim()
        return DATE_PATTERNS.firstNotNullOfOrNull { pattern ->
            DATE_LOCALES.firstNotNullOfOrNull { locale ->
                runCatching {
                    SimpleDateFormat(pattern, locale).apply {
                        isLenient = false
                    }.parse(trimmedValue)
                }.getOrNull()
            }
        }
    }

    private fun Calendar.isInLastSevenDays(anchor: Calendar): Boolean {
        val start = anchor.copyAtStartOfDay().apply { add(Calendar.DAY_OF_YEAR, -6) }
        val end = anchor.copyAtStartOfDay().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return !before(start) && !after(end)
    }

    private fun Calendar.copyAtStartOfDay(): Calendar {
        return (clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private companion object {
        val DATE_LOCALES = listOf(Locale.US, Locale.ENGLISH, Locale.forLanguageTag("vi-VN"))
        val DATE_PATTERNS = listOf(
            "dd MMM yyyy",
            "d MMM yyyy",
            "dd MMMM yyyy",
            "d MMMM yyyy",
            "dd/MM/yyyy",
            "d/M/yyyy",
            "dd-MM-yyyy",
            "d-M-yyyy",
            "yyyy-MM-dd",
            "EEE MMM dd HH:mm:ss zzz yyyy"
        )
    }
}

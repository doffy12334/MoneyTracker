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
        val expenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
        val anchorDate = expenses.periodAnchorCalendar()
        val filteredExpenses = expenses
            .filter { it.matchesPeriod(period, anchorDate) }

        val totalSpent = filteredExpenses.sumOf { it.amount }
        val breakdown = filteredExpenses
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

    private fun Transaction.matchesPeriod(period: ReportPeriod, anchorDate: Calendar): Boolean {
        val date = parseReportDate() ?: return false
        val transactionDate = Calendar.getInstance().apply { time = date }
        return when (period) {
            ReportPeriod.WEEKLY -> transactionDate.isInSameWeek(anchorDate)
            ReportPeriod.MONTHLY ->
                transactionDate.get(Calendar.YEAR) == anchorDate.get(Calendar.YEAR) &&
                    transactionDate.get(Calendar.MONTH) == anchorDate.get(Calendar.MONTH)
            ReportPeriod.YEARLY ->
                transactionDate.get(Calendar.YEAR) == anchorDate.get(Calendar.YEAR)
        }
    }

    private fun List<Transaction>.periodAnchorCalendar(): Calendar {
        val today = Calendar.getInstance()
        val endOfToday = today.copyAtEndOfDay()
        val latestAvailableDate = mapNotNull { it.parseReportDate() }
            .filter { !it.after(endOfToday.time) }
            .maxByOrNull { it.time }
        return Calendar.getInstance().apply { time = latestAvailableDate ?: today.time }
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

    private fun Calendar.isInSameWeek(anchor: Calendar): Boolean {
        firstDayOfWeek = Calendar.MONDAY
        minimalDaysInFirstWeek = 4
        val normalizedAnchor = (anchor.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
        return getWeekYear() == normalizedAnchor.getWeekYear() &&
            get(Calendar.WEEK_OF_YEAR) == normalizedAnchor.get(Calendar.WEEK_OF_YEAR)
    }

    private fun Calendar.copyAtEndOfDay(): Calendar {
        return (clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
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
            "yyyy-MM-dd HH:mm:ss.SSS",
            "EEE MMM dd HH:mm:ss zzz yyyy"
        )
    }
}

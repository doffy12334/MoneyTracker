package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.ExportPeriod
import com.example.moneytracker.domain.model.ExportReportRequest
import com.example.moneytracker.domain.model.ExportReportResult
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.repository.ExportReportRepository
import com.example.moneytracker.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExportReportUseCase(
    private val transactionRepository: TransactionRepository,
    private val exportReportRepository: ExportReportRepository
) {
    suspend operator fun invoke(request: ExportReportRequest): ExportReportResult {
        val transactions = transactionRepository.getTransactions()
            .filter { it.matchesExportPeriod(request) }
        return exportReportRepository.exportReport(request, transactions)
    }

    private fun Transaction.matchesExportPeriod(request: ExportReportRequest): Boolean {
        val transactionDate = parseExportDate() ?: return false
        if (request.period == ExportPeriod.CUSTOM) {
            val startDate = request.customStartDate?.let(::parseDate) ?: return false
            val endDate = request.customEndDate?.let(::parseDate) ?: return false
            return !transactionDate.before(startDate.startOfDay()) &&
                !transactionDate.after(endDate.endOfDay())
        }
        val transactionCalendar = Calendar.getInstance().apply { time = transactionDate }
        val targetCalendar = Calendar.getInstance().apply {
            if (request.period == ExportPeriod.LAST_MONTH) {
                add(Calendar.MONTH, -1)
            }
        }
        return transactionCalendar.get(Calendar.YEAR) == targetCalendar.get(Calendar.YEAR) &&
            transactionCalendar.get(Calendar.MONTH) == targetCalendar.get(Calendar.MONTH)
    }

    private fun Transaction.parseExportDate(): Date? {
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

    private fun Date.startOfDay(): Date {
        return Calendar.getInstance().apply {
            time = this@startOfDay
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun Date.endOfDay(): Date {
        return Calendar.getInstance().apply {
            time = this@endOfDay
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
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

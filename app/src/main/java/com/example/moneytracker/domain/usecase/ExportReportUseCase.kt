package com.example.moneytracker.domain.usecase

import com.example.moneytracker.domain.model.ExportReportRequest
import com.example.moneytracker.domain.model.ExportReportResult
import com.example.moneytracker.domain.repository.ExportReportRepository
import com.example.moneytracker.domain.repository.TransactionRepository

class ExportReportUseCase(
    private val transactionRepository: TransactionRepository,
    private val exportReportRepository: ExportReportRepository
) {
    suspend operator fun invoke(request: ExportReportRequest): ExportReportResult {
        val transactions = transactionRepository.getTransactions()
        return exportReportRepository.exportReport(request, transactions)
    }
}

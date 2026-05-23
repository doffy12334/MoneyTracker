package com.example.moneytracker.domain.repository

import com.example.moneytracker.domain.model.ExportReportRequest
import com.example.moneytracker.domain.model.ExportReportResult
import com.example.moneytracker.domain.model.transaction.Transaction

interface ExportReportRepository {
    fun exportReport(
        request: ExportReportRequest,
        transactions: List<Transaction>
    ): ExportReportResult
}

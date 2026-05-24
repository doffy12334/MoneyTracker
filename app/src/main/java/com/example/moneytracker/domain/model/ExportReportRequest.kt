package com.example.moneytracker.domain.model

enum class ExportPeriod {
    CURRENT_MONTH,
    LAST_MONTH,
    CUSTOM
}

enum class ExportFileFormat {
    CSV,
    PDF
}

data class ExportReportRequest(
    val period: ExportPeriod,
    val fileFormat: ExportFileFormat,
    val customStartDate: String? = null,
    val customEndDate: String? = null
)

data class ExportReportResult(
    val fileName: String,
    val filePath: String,
    val transactionCount: Int
)

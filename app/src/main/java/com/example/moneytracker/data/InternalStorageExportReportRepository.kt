package com.example.moneytracker.data

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.moneytracker.domain.model.report.ExportFileFormat
import com.example.moneytracker.domain.model.report.ExportPeriod
import com.example.moneytracker.domain.model.report.ExportReportRequest
import com.example.moneytracker.domain.model.report.ExportReportResult
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.repository.ExportReportRepository
import java.io.File
import java.io.FileOutputStream

class InternalStorageExportReportRepository(
    context: Context
) : ExportReportRepository {
    private val reportsDir = File(context.filesDir, REPORTS_DIR).apply { mkdirs() }

    override fun exportReport(
        request: ExportReportRequest,
        transactions: List<Transaction>
    ): ExportReportResult {
        val fileName = buildFileName(request)
        val file = File(reportsDir, fileName)
        when (request.fileFormat) {
            ExportFileFormat.CSV -> writeCsv(file, transactions)
            ExportFileFormat.PDF -> writePdf(file, transactions)
        }
        return ExportReportResult(
            fileName = fileName,
            filePath = file.absolutePath,
            transactionCount = transactions.size
        )
    }

    private fun buildFileName(request: ExportReportRequest): String {
        val period = when (request.period) {
            ExportPeriod.CURRENT_MONTH -> "current_month"
            ExportPeriod.LAST_MONTH -> "last_month"
            ExportPeriod.CUSTOM -> "custom"
        }
        val extension = when (request.fileFormat) {
            ExportFileFormat.CSV -> "csv"
            ExportFileFormat.PDF -> "pdf"
        }
        return "money_tracker_${period}_report.$extension"
    }

    private fun writeCsv(file: File, transactions: List<Transaction>) {
        val content = buildString {
            appendLine("id,name,amount,date,category,type")
            transactions.forEach { transaction ->
                appendLine(
                    listOf(
                        transaction.id,
                        transaction.name,
                        transaction.amount.toString(),
                        transaction.date,
                        transaction.category.name,
                        transaction.type.name
                    ).joinToString(",") { it.escapeCsv() }
                )
            }
        }
        file.writeText(content)
    }

    private fun writePdf(file: File, transactions: List<Transaction>) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
        }

        canvas.drawText("Money Tracker Report", 40f, 48f, titlePaint)
        canvas.drawText("Total transactions: ${transactions.size}", 40f, 72f, bodyPaint)

        var y = 104f
        transactions.take(28).forEach { transaction ->
            canvas.drawText(
                "${transaction.date}  ${transaction.name}  ${transaction.amount}  ${transaction.type}",
                40f,
                y,
                bodyPaint
            )
            y += 24f
        }

        document.finishPage(page)
        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()
    }

    private fun String.escapeCsv(): String {
        val escaped = replace("\"", "\"\"")
        return if (contains(",") || contains("\"") || contains("\n")) "\"$escaped\"" else escaped
    }

    private companion object {
        const val REPORTS_DIR = "reports"
    }
}

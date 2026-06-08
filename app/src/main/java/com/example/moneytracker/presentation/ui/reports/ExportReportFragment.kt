package com.example.moneytracker.presentation.ui.reports

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentExportReportBinding
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.ExportFileFormat
import com.example.moneytracker.domain.model.ExportPeriod
import com.example.moneytracker.domain.model.ExportReportResult
import com.example.moneytracker.presentation.uistate.ExportReportUiState
import com.example.moneytracker.presentation.viewmodel.ExportReportViewModel
import java.io.FileOutputStream
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExportReportFragment : Fragment() {
    private var _binding: FragmentExportReportBinding? = null
    private val binding get() = _binding!!
    private var pendingReport: ExportReportResult? = null
    private var pendingAction = ExportAction.DOWNLOAD

    private val createReportDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        val report = pendingReport
        pendingReport = null
        if (result.resultCode == Activity.RESULT_OK && uri != null && report != null) {
            saveReportToUri(report, uri)
        }
    }

    private val viewModel: ExportReportViewModel by viewModels {
        ExportReportViewModel.Factory(AppContainer.exportReportUseCase)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        renderPeriodLabels()
        binding.optionCurrentMonth.setOnClickListener {
            viewModel.selectPeriod(ExportPeriod.CURRENT_MONTH)
        }
        binding.optionLastMonth.setOnClickListener {
            viewModel.selectPeriod(ExportPeriod.LAST_MONTH)
        }
        binding.optionCustom.setOnClickListener {
            showCustomDateRangePicker()
        }
        binding.optionCsv.setOnClickListener {
            viewModel.selectFormat(ExportFileFormat.CSV)
        }
        binding.optionPdf.setOnClickListener {
            viewModel.selectFormat(ExportFileFormat.PDF)
        }
        binding.btnExportReport.setOnClickListener {
            pendingAction = ExportAction.DOWNLOAD
            viewModel.exportReport()
        }
        binding.btnPrintReport.setOnClickListener {
            pendingAction = ExportAction.PRINT
            viewModel.exportReport()
        }
        binding.btnShareReport.setOnClickListener {
            pendingAction = ExportAction.SHARE
            viewModel.exportReport()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: ExportReportUiState) {
        TransitionManager.beginDelayedTransition(
            binding.root,
            AutoTransition().apply { duration = SELECTION_ANIMATION_DURATION_MS }
        )
        renderPeriodLabels()
        setSelected(binding.optionCurrentMonth, state.selectedPeriod == ExportPeriod.CURRENT_MONTH)
        setSelected(binding.optionLastMonth, state.selectedPeriod == ExportPeriod.LAST_MONTH)
        setSelected(binding.optionCustom, state.selectedPeriod == ExportPeriod.CUSTOM)
        setSelected(binding.optionCsv, state.selectedFormat == ExportFileFormat.CSV)
        setSelected(binding.optionPdf, state.selectedFormat == ExportFileFormat.PDF)
        binding.btnExportReport.isEnabled = !state.isExporting
        binding.btnPrintReport.isEnabled = !state.isExporting
        binding.btnShareReport.isEnabled = !state.isExporting
        binding.btnPrintReport.visibility = if (state.selectedFormat == ExportFileFormat.PDF) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.btnExportReport.text = getString(
            if (state.isExporting) {
                R.string.export_report_exporting
            } else {
                R.string.export_report_action
            }
        )

        val message = state.errorMessage?.let { error ->
            if (error == CUSTOM_DATE_REQUIRED_ERROR) getString(R.string.export_custom_date_required) else error
        } ?: state.successMessage
        if (message != null) {
            Toast.makeText(
                requireContext(),
                message.ifBlank { getString(R.string.export_report_error) },
                Toast.LENGTH_SHORT
            ).show()
            viewModel.consumeMessage()
        }

        state.exportedReport?.let { report ->
            handleExportedReport(report)
            viewModel.consumeExportedReport()
        }
    }

    private fun handleExportedReport(report: ExportReportResult) {
        Toast.makeText(
            requireContext(),
            getString(R.string.export_report_created, report.fileName, report.transactionCount),
            Toast.LENGTH_SHORT
        ).show()
        when (pendingAction) {
            ExportAction.DOWNLOAD -> openDownloadPicker(report)
            ExportAction.PRINT -> printReport(report)
            ExportAction.SHARE -> shareReport(report)
        }
        pendingAction = ExportAction.DOWNLOAD
    }

    private fun setSelected(view: TextView, selected: Boolean) {
        view.setBackgroundResource(
            if (selected) R.drawable.bg_export_option_selected else R.drawable.bg_soft_green_card
        )
        view.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (selected) R.color.text_primary else R.color.text_secondary
            )
        )
        view.typeface = Typeface.DEFAULT_BOLD
        view.alpha = if (selected) 1f else 0.82f
    }

    private fun renderPeriodLabels() {
        val currentMonth = Calendar.getInstance()
        val lastMonth = (currentMonth.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        binding.optionCurrentMonth.text = getString(
            R.string.export_period_current_month,
            currentMonth.toMonthRangeLabel()
        )
        binding.optionLastMonth.text = getString(
            R.string.export_period_last_month,
            lastMonth.toMonthRangeLabel()
        )
        val state = viewModel.uiState.value
        binding.optionCustom.text = if (
            !state.customStartDate.isNullOrBlank() &&
            !state.customEndDate.isNullOrBlank()
        ) {
            getString(
                R.string.export_period_custom_selected,
                state.customStartDate,
                state.customEndDate
            )
        } else {
            getString(R.string.export_period_custom)
        }
    }

    private fun showCustomDateRangePicker() {
        val today = Calendar.getInstance()
        showDatePicker(getString(R.string.export_pick_start_date), today) { startDate ->
            showDatePicker(getString(R.string.export_pick_end_date), startDate) { endDate ->
                val start = startDate.copy()
                val end = endDate.copy()
                if (start.after(end)) {
                    viewModel.selectCustomDateRange(end.toExportDate(), start.toExportDate())
                } else {
                    viewModel.selectCustomDateRange(start.toExportDate(), end.toExportDate())
                }
            }
        }
    }

    private fun showDatePicker(
        title: String,
        initialDate: Calendar,
        onDateSelected: (Calendar) -> Unit
    ) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                onDateSelected(
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }
                )
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle(title)
        }.show()
    }

    private fun Calendar.toMonthRangeLabel(): String {
        val formatter = SimpleDateFormat("dd/MM", currentLocale())
        val start = (clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val end = (clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        return "${formatter.format(start.time)} - ${formatter.format(end.time)}"
    }

    private fun Calendar.toExportDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", currentLocale()).format(time)
    }

    private fun Calendar.copy(): Calendar {
        return clone() as Calendar
    }

    private fun currentLocale(): Locale {
        return resources.configuration.locales[0] ?: Locale.getDefault()
    }

    private fun openDownloadPicker(report: ExportReportResult) {
        pendingReport = report
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = report.mimeType()
            putExtra(Intent.EXTRA_TITLE, report.fileName)
        }
        createReportDocumentLauncher.launch(intent)
    }

    private fun saveReportToUri(report: ExportReportResult, uri: Uri) {
        runCatching {
            val sourceFile = File(report.filePath)
            sourceFile.inputStream().use { input ->
                val output = requireContext().contentResolver.openOutputStream(uri)
                    ?: error(getString(R.string.export_report_open_file_error))
                output.use { input.copyTo(it) }
            }
        }.onSuccess {
            Toast.makeText(
                requireContext(),
                getString(R.string.export_report_downloaded, report.fileName),
                Toast.LENGTH_SHORT
            ).show()
        }.onFailure { exception ->
            Toast.makeText(
                requireContext(),
                exception.message ?: getString(R.string.export_report_download_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun printReport(report: ExportReportResult) {
        if (report.fileName.substringAfterLast('.', "").lowercase() != "pdf") {
            Toast.makeText(requireContext(), R.string.export_report_print_pdf_only, Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            Toast.makeText(requireContext(), R.string.export_report_printing, Toast.LENGTH_SHORT).show()
            val printManager = requireContext().getSystemService(PrintManager::class.java)
            val reportFile = File(report.filePath)
            printManager.print(
                report.fileName.substringBeforeLast('.'),
                PdfFilePrintAdapter(reportFile, report.fileName),
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .build()
            )
        }.onFailure {
            Toast.makeText(requireContext(), R.string.export_report_print_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareReport(report: ExportReportResult) {
        runCatching {
            val reportFile = File(report.filePath)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                reportFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = report.mimeType()
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, report.fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.export_report_share_title)))
        }.onFailure {
            Toast.makeText(requireContext(), R.string.export_report_share_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun ExportReportResult.mimeType(): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "csv" -> "text/csv"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val SELECTION_ANIMATION_DURATION_MS = 120L
        const val CUSTOM_DATE_REQUIRED_ERROR = "CUSTOM_DATE_REQUIRED"
    }

    private enum class ExportAction {
        DOWNLOAD,
        PRINT,
        SHARE
    }

    private class PdfFilePrintAdapter(
        private val file: File,
        private val fileName: String
    ) : PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder(fileName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback
        ) {
            runCatching {
                FileInputStream(file).use { input ->
                    FileOutputStream(destination.fileDescriptor).use { output ->
                        input.copyTo(output)
                    }
                }
            }.onSuccess {
                if (cancellationSignal.isCanceled) {
                    callback.onWriteCancelled()
                } else {
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                }
            }.onFailure { exception ->
                callback.onWriteFailed(exception.message)
            }
        }
    }
}

package com.example.moneytracker.presentation.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.moneytracker.R
import com.example.moneytracker.di.AppContainer
import com.example.moneytracker.domain.model.transaction.Transaction
import com.example.moneytracker.domain.model.transaction.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class AppNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppContainer.init(context.applicationContext)
        AppNotificationScheduler(context.applicationContext).scheduleAll()
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (AppContainer.getSettingsUseCase().notificationsEnabled) {
                    handleAction(context.applicationContext, intent.action.orEmpty())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAction(context: Context, action: String) {
        when (action) {
            ACTION_MEAL_REMINDER_LUNCH -> showMealReminder(context, R.string.notification_reminder_lunch_message, 1301)
            ACTION_MEAL_REMINDER_DINNER -> showMealReminder(context, R.string.notification_reminder_dinner_message, 1302)
            ACTION_DAILY_NO_ENTRY_REMINDER -> showDailyNoEntryReminder(context)
            ACTION_DAILY_SPENDING_REVIEW -> showDailySpendingReview(context)
            ACTION_SAVING_GOAL_PROGRESS -> showSavingGoalProgress(context)
            ACTION_WEEKLY_DIGEST -> showWeeklyDigest(context)
            ACTION_MONTHLY_DIGEST -> showMonthlyDigest(context)
            Intent.ACTION_BOOT_COMPLETED -> AppNotificationScheduler(context).scheduleAll()
        }
    }

    private fun showMealReminder(context: Context, messageRes: Int, notificationId: Int) {
        BudgetNotificationHelper(context).showReminder(
            title = context.getString(R.string.notification_reminder_title),
            message = context.getString(messageRes),
            notificationId = notificationId
        )
    }

    private suspend fun showDailyNoEntryReminder(context: Context) {
        val hasEntryToday = AppContainer.getTransactionsUseCase()
            .any { it.parsedDate()?.isToday() == true }
        if (hasEntryToday) return
        BudgetNotificationHelper(context).showReminder(
            title = context.getString(R.string.notification_reminder_title),
            message = context.getString(R.string.notification_reminder_no_entry_message),
            notificationId = 1303
        )
    }

    private suspend fun showDailySpendingReview(context: Context) {
        val transactions = AppContainer.getTransactionsUseCase()
        val today = Calendar.getInstance()
        val todayExpense = transactions
            .filter { it.type == TransactionType.EXPENSE && it.parsedDate()?.isSameDay(today) == true }
            .sumOf { it.amount }
        if (todayExpense <= 0.0) return

        val previousSevenDayExpense = transactions
            .filter { it.type == TransactionType.EXPENSE && it.parsedDate()?.isWithinPreviousDays(today, 7) == true }
            .sumOf { it.amount }
        val averagePreviousExpense = previousSevenDayExpense / 7.0
        val currency = AppContainer.getSettingsUseCase().currency
        val message = if (averagePreviousExpense > 0.0 && todayExpense >= averagePreviousExpense * HIGH_DAILY_SPENDING_MULTIPLIER) {
            context.getString(
                R.string.notification_daily_spending_high_message,
                CurrencyFormatter.formatFromVnd(todayExpense, currency),
                CurrencyFormatter.formatFromVnd(averagePreviousExpense, currency)
            )
        } else {
            context.getString(R.string.notification_daily_spending_review_message, CurrencyFormatter.formatFromVnd(todayExpense, currency))
        }

        BudgetNotificationHelper(context).showReminder(
            title = context.getString(R.string.notification_daily_spending_review_title),
            message = message,
            notificationId = 1401
        )
    }

    private suspend fun showSavingGoalProgress(context: Context) {
        val today = Calendar.getInstance()
        if (today.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) return

        val goal = AppContainer.getBudgetOverviewUseCase()
            .savingGoals
            .filterNot { it.isCompleted }
            .minByOrNull { it.remainingAmount }
            ?: return
        val currency = AppContainer.getSettingsUseCase().currency
        BudgetNotificationHelper(context).showReminder(
            title = context.getString(R.string.notification_saving_goal_title),
            message = context.getString(
                R.string.notification_saving_goal_message,
                goal.goal.title,
                CurrencyFormatter.formatFromVnd(goal.remainingAmount, currency)
            ),
            notificationId = 1402
        )
    }

    private suspend fun showWeeklyDigest(context: Context) {
        val today = Calendar.getInstance()
        if (today.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) return
        val weekKey = SimpleDateFormat("yyyy-'W'ww", Locale.US).format(today.time)
        val tracker = NotificationStateTracker(context)
        if (!tracker.shouldNotifyDigest("week_$weekKey")) return

        val transactions = AppContainer.getTransactionsUseCase()
        val currentSavings = transactions.savingsForWeek(0)
        val previousSavings = transactions.savingsForWeek(-1)
        val message = if (previousSavings > 0.0) {
            val changePercent = (((currentSavings - previousSavings) / previousSavings) * 100).roundToInt()
            context.getString(R.string.notification_weekly_digest_message_with_change, changePercent)
        } else {
            context.getString(R.string.notification_weekly_digest_message)
        }
        BudgetNotificationHelper(context).showDigest(
            title = context.getString(R.string.notification_weekly_digest_title),
            message = message,
            notificationId = 1501
        )
    }

    private suspend fun showMonthlyDigest(context: Context) {
        val today = Calendar.getInstance()
        if (today.get(Calendar.DAY_OF_MONTH) != today.getActualMaximum(Calendar.DAY_OF_MONTH)) return
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(today.time)
        val tracker = NotificationStateTracker(context)
        if (!tracker.shouldNotifyDigest("month_$monthKey")) return

        val transactions = AppContainer.getTransactionsUseCase()
        val expenseThisMonth = transactions
            .filter { it.type == TransactionType.EXPENSE && it.parsedDate()?.isSameMonth(today) == true }
            .sumOf { it.amount }
        val currency = AppContainer.getSettingsUseCase().currency
        BudgetNotificationHelper(context).showDigest(
            title = context.getString(R.string.notification_monthly_digest_title),
            message = context.getString(
                R.string.notification_monthly_digest_message,
                CurrencyFormatter.formatFromVnd(expenseThisMonth, currency)
            ),
            notificationId = 1502
        )
    }

    private fun List<Transaction>.savingsForWeek(weekOffset: Int): Double {
        val anchor = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, weekOffset)
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
        val income = filter { it.type == TransactionType.INCOME && it.parsedDate()?.isSameWeek(anchor) == true }
            .sumOf { it.amount }
        val expense = filter { it.type == TransactionType.EXPENSE && it.parsedDate()?.isSameWeek(anchor) == true }
            .sumOf { it.amount }
        return income - expense
    }

    private fun Transaction.parsedDate(): Date? = parseDate(date) ?: parseDate(createdAt)

    private fun Date.isToday(): Boolean {
        val now = Calendar.getInstance()
        return isSameDay(now)
    }

    private fun Date.isSameDay(anchor: Calendar): Boolean {
        val calendar = Calendar.getInstance().apply { time = this@isSameDay }
        return calendar.get(Calendar.YEAR) == anchor.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == anchor.get(Calendar.DAY_OF_YEAR)
    }

    private fun Date.isWithinPreviousDays(anchor: Calendar, days: Int): Boolean {
        val start = (anchor.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (anchor.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val time = time
        return time >= start.timeInMillis && time <= end.timeInMillis
    }

    private fun Date.isSameMonth(anchor: Calendar): Boolean {
        val calendar = Calendar.getInstance().apply { time = this@isSameMonth }
        return calendar.get(Calendar.YEAR) == anchor.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == anchor.get(Calendar.MONTH)
    }

    private fun Date.isSameWeek(anchor: Calendar): Boolean {
        val calendar = Calendar.getInstance().apply {
            time = this@isSameWeek
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
        return calendar.getWeekYear() == anchor.getWeekYear() &&
            calendar.get(Calendar.WEEK_OF_YEAR) == anchor.get(Calendar.WEEK_OF_YEAR)
    }

    private fun parseDate(value: String): Date? {
        val trimmedValue = value.trim()
        if (trimmedValue.isBlank()) return null
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

    companion object {
        const val ACTION_MEAL_REMINDER_LUNCH = "com.example.moneytracker.action.MEAL_REMINDER_LUNCH"
        const val ACTION_MEAL_REMINDER_DINNER = "com.example.moneytracker.action.MEAL_REMINDER_DINNER"
        const val ACTION_DAILY_NO_ENTRY_REMINDER = "com.example.moneytracker.action.DAILY_NO_ENTRY_REMINDER"
        const val ACTION_DAILY_SPENDING_REVIEW = "com.example.moneytracker.action.DAILY_SPENDING_REVIEW"
        const val ACTION_SAVING_GOAL_PROGRESS = "com.example.moneytracker.action.SAVING_GOAL_PROGRESS"
        const val ACTION_WEEKLY_DIGEST = "com.example.moneytracker.action.WEEKLY_DIGEST"
        const val ACTION_MONTHLY_DIGEST = "com.example.moneytracker.action.MONTHLY_DIGEST"

        private const val HIGH_DAILY_SPENDING_MULTIPLIER = 1.5
        private val DATE_LOCALES = listOf(Locale.US, Locale.ENGLISH, Locale.forLanguageTag("vi-VN"))
        private val DATE_PATTERNS = listOf(
            "dd/MM/yyyy",
            "d/M/yyyy",
            "dd-MM-yyyy",
            "d-M-yyyy",
            "yyyy-MM-dd",
            "dd MMM yyyy",
            "d MMM yyyy",
            "dd MMMM yyyy",
            "d MMMM yyyy",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "EEE MMM dd HH:mm:ss zzz yyyy"
        )
    }
}

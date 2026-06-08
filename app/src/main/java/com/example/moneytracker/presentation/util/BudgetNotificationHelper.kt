package com.example.moneytracker.presentation.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.moneytracker.R
import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.presentation.ui.activities.MainActivity

class BudgetNotificationHelper(
    private val context: Context
) {
    fun showBudgetExceeded(
        category: TransactionCategory,
        spentAmount: Double,
        limitAmount: Double,
        currency: AppCurrency,
        threshold: Int = 100
    ) {
        if (!canNotify()) return
        createChannel()
        val categoryName = category.localizedName()
        val message = context.getString(
            if (threshold >= 100) {
                R.string.notification_budget_exceeded_message
            } else {
                R.string.notification_budget_threshold_message
            },
            categoryName,
            CurrencyFormatter.formatFromVnd(spentAmount, currency),
            CurrencyFormatter.formatFromVnd(limitAmount, currency)
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wallet)
            .setContentTitle(context.getString(R.string.notification_budget_exceeded_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainActivityIntent())
            .build()

        showNotification(NOTIFICATION_ID_BUDGET_BASE + category.ordinal + threshold, notification)
    }

    fun showReminder(title: String, message: String, notificationId: Int) {
        showSimpleNotification(title, message, notificationId, NotificationCompat.PRIORITY_DEFAULT)
    }

    fun showDigest(title: String, message: String, notificationId: Int) {
        showSimpleNotification(title, message, notificationId, NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun showSimpleNotification(
        title: String,
        message: String,
        notificationId: Int,
        priority: Int
    ) {
        if (!canNotify()) return
        createChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wallet)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(mainActivityIntent())
            .build()

        showNotification(notificationId, notification)
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(notificationId: Int, notification: android.app.Notification) {
        if (!canNotify()) return
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_budget_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_budget_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun mainActivityIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canNotify(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun TransactionCategory.localizedName(): String {
        return context.getString(
            when (this) {
                TransactionCategory.FOOD -> R.string.category_food
                TransactionCategory.TRANSPORT -> R.string.category_transport
                TransactionCategory.SHOPPING -> R.string.category_shopping
                TransactionCategory.SALARY -> R.string.category_salary
                TransactionCategory.ENTERTAINMENT -> R.string.category_entertainment
                TransactionCategory.OTHER -> R.string.category_other
            }
        )
    }

    private companion object {
        const val CHANNEL_ID = "budget_alerts"
        const val NOTIFICATION_ID_BUDGET_BASE = 1200
    }
}

package com.example.moneytracker.presentation.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class AppNotificationScheduler(
    private val context: Context
) {
    fun scheduleAll() {
        scheduleRepeating(AppNotificationReceiver.ACTION_MEAL_REMINDER_LUNCH, 12, 30, REQUEST_LUNCH)
        scheduleRepeating(AppNotificationReceiver.ACTION_MEAL_REMINDER_DINNER, 19, 30, REQUEST_DINNER)
        scheduleRepeating(AppNotificationReceiver.ACTION_DAILY_NO_ENTRY_REMINDER, 21, 0, REQUEST_DAILY)
        scheduleRepeating(AppNotificationReceiver.ACTION_DAILY_SPENDING_REVIEW, 20, 45, REQUEST_DAILY_SPENDING)
        scheduleRepeating(AppNotificationReceiver.ACTION_SAVING_GOAL_PROGRESS, 10, 0, REQUEST_SAVING_GOALS, Calendar.SATURDAY)
        scheduleRepeating(AppNotificationReceiver.ACTION_WEEKLY_DIGEST, 20, 0, REQUEST_WEEKLY, Calendar.SUNDAY)
        scheduleRepeating(AppNotificationReceiver.ACTION_MONTHLY_DIGEST, 20, 30, REQUEST_MONTHLY)
    }

    private fun scheduleRepeating(
        action: String,
        hour: Int,
        minute: Int,
        requestCode: Int,
        dayOfWeek: Int? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTrigger(hour, minute, dayOfWeek).timeInMillis
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(action, requestCode)
        )
    }

    private fun pendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AppNotificationReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTrigger(hour: Int, minute: Int, dayOfWeek: Int?): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            dayOfWeek?.let { targetDay ->
                while (get(Calendar.DAY_OF_WEEK) != targetDay) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, if (dayOfWeek == null) 1 else 7)
            }
        }
    }

    private companion object {
        const val REQUEST_LUNCH = 2001
        const val REQUEST_DINNER = 2002
        const val REQUEST_DAILY = 2003
        const val REQUEST_DAILY_SPENDING = 2004
        const val REQUEST_SAVING_GOALS = 2007
        const val REQUEST_WEEKLY = 2005
        const val REQUEST_MONTHLY = 2006
    }
}

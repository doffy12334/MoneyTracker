package com.example.moneytracker.presentation.util

import android.content.Context
import androidx.core.content.edit
import com.example.moneytracker.domain.model.transaction.TransactionCategory

class NotificationStateTracker(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun shouldNotifyBudgetThreshold(
        category: TransactionCategory,
        monthKey: String,
        threshold: Int
    ): Boolean {
        val key = "budget_${category.name}_${monthKey}_$threshold"
        if (sharedPreferences.getBoolean(key, false)) return false
        sharedPreferences.edit { putBoolean(key, true) }
        return true
    }

    fun shouldNotifyDigest(periodKey: String): Boolean {
        val key = "digest_$periodKey"
        if (sharedPreferences.getBoolean(key, false)) return false
        sharedPreferences.edit { putBoolean(key, true) }
        return true
    }

    private companion object {
        const val PREF_NAME = "money_tracker_notification_state"
    }
}

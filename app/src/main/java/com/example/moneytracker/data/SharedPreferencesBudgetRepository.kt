package com.example.moneytracker.data

import android.content.Context
import androidx.core.content.edit
import com.example.moneytracker.domain.model.budget.BudgetLimit
import com.example.moneytracker.domain.model.budget.SavingGoal
import com.example.moneytracker.domain.model.transaction.TransactionCategory
import com.example.moneytracker.domain.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

class SharedPreferencesBudgetRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : BudgetRepository {
    private val sharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE)

    override suspend fun getBudgetLimits(): List<BudgetLimit> {
        val rawLimits = sharedPreferences.getString(
            currentBudgetLimitsKey(),
            null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(rawLimits)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(index) ?: continue
                    val category = runCatching {
                        enumValueOf<TransactionCategory>(

                            jsonObject.getString("category"))
                    }.getOrNull() ?: continue
                    val amount = jsonObject.optDouble("limitAmount", 0.0)
                    if (amount > 0.0) add(BudgetLimit(category, amount))
                }
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun saveBudgetLimit(limit: BudgetLimit) {
        val nextLimits = getBudgetLimits()
            .filterNot { it.category == limit.category } + limit
        sharedPreferences.edit {
            putString(
                currentBudgetLimitsKey(),
                nextLimits.sortedBy { it.category.name }.toBudgetJsonArray().toString())
        }
    }

    override suspend fun deleteBudgetLimit(category: TransactionCategory) {
        sharedPreferences.edit {
            putString(
                currentBudgetLimitsKey(),
                getBudgetLimits().filterNot { it.category == category }
                    .toBudgetJsonArray()
                    .toString()
            )
        }
    }

    override suspend fun getSavingGoals(): List<SavingGoal> {
        val rawGoals =
            sharedPreferences.getString(currentSavingGoalsKey(), null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(rawGoals)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(index) ?: continue
                    val title = jsonObject.optString("title").trim()
                    val targetAmount = jsonObject.optDouble("targetAmount", 0.0)
                    if (title.isBlank() || targetAmount <= 0.0) continue
                    add(
                        SavingGoal(
                            id = jsonObject.optString("id", title),
                            title = title,
                            targetAmount = targetAmount,
                            currentAmount = jsonObject.optDouble("currentAmount", 0.0)
                                .coerceAtLeast(0.0)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun saveSavingGoal(goal: SavingGoal) {
        val nextGoals = getSavingGoals()
            .filterNot { it.id == goal.id } + goal
        sharedPreferences.edit {
            putString(currentSavingGoalsKey(), nextGoals.toSavingJsonArray().toString())
        }
    }

    override suspend fun deleteSavingGoal(goalId: String) {
        sharedPreferences.edit {
            putString(
                currentSavingGoalsKey(),
                getSavingGoals().filterNot { it.id == goalId }.toSavingJsonArray().toString()
            )
        }
    }

    private fun currentBudgetLimitsKey(): String = "${KEY_BUDGET_LIMITS_PREFIX}${currentUserId()}"

    private fun currentSavingGoalsKey(): String = "${KEY_SAVING_GOALS_PREFIX}${currentUserId()}"

    private fun currentUserId(): String = auth.currentUser?.uid ?: GUEST_USER_ID

    private fun List<BudgetLimit>.toBudgetJsonArray(): JSONArray {
        return JSONArray().apply {
            forEach { limit ->
                put(
                    JSONObject()
                        .put("category", limit.category.name)
                        .put("limitAmount", limit.limitAmount)
                )
            }
        }
    }

    private fun List<SavingGoal>.toSavingJsonArray(): JSONArray {
        return JSONArray().apply {
            forEach { goal ->
                put(
                    JSONObject()
                        .put("id", goal.id)
                        .put("title", goal.title)
                        .put("targetAmount", goal.targetAmount)
                        .put("currentAmount", goal.currentAmount)
                )
            }
        }
    }

    private companion object {
        const val PREF_NAME = "money_tracker_budgets"
        const val KEY_BUDGET_LIMITS_PREFIX = "budget_limits_"
        const val KEY_SAVING_GOALS_PREFIX = "saving_goals_"
        const val GUEST_USER_ID = "guest"
    }
}

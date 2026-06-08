package com.example.moneytracker.data

import com.example.moneytracker.data.local.SharedPrefsManager
import com.example.moneytracker.domain.model.AppCurrency
import com.example.moneytracker.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class OpenExchangeRateRepository(
    private val sharedPrefsManager: SharedPrefsManager
) : ExchangeRateRepository {
    override suspend fun getVndPerUnitRates(forceRefresh: Boolean): Map<AppCurrency, Double> {
        val cachedRates = readCachedRates()
        if (!forceRefresh && cachedRates.isNotEmpty() && !isCacheExpired()) {
            return cachedRates
        }

        return runCatching {
            fetchLiveRates()
        }.getOrElse {
            cachedRates.ifEmpty { fallbackRates() }
        }
    }

    private suspend fun fetchLiveRates(): Map<AppCurrency, Double> = withContext(Dispatchers.IO) {
        val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
        }

        try {
            require(connection.responseCode in 200..299) {
                "Exchange rate request failed: ${connection.responseCode}"
            }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val payload = JSONObject(response)
            require(payload.optString("result") == "success") { "Exchange rate response is not successful" }

            val conversionRates = payload.getJSONObject("rates")
            val vndPerUnitRates = AppCurrency.entries.associateWith { currency ->
                if (currency == AppCurrency.VND) {
                    1.0
                } else {
                    val oneVndToCurrency = conversionRates.getDouble(currency.code)
                    1.0 / oneVndToCurrency
                }
            }
            sharedPrefsManager.setExchangeRatesJson(
                JSONObject().apply {
                    vndPerUnitRates.forEach { (currency, rate) -> put(currency.code, rate) }
                }.toString(),
                System.currentTimeMillis()
            )
            vndPerUnitRates
        } finally {
            connection.disconnect()
        }
    }

    private fun readCachedRates(): Map<AppCurrency, Double> {
        val rawRates = sharedPrefsManager.getExchangeRatesJson() ?: return emptyMap()
        return runCatching {
            val json = JSONObject(rawRates)
            AppCurrency.entries.associateWith { currency ->
                json.optDouble(currency.code, currency.fallbackVndPerUnit)
            }
        }.getOrDefault(emptyMap())
    }

    private fun isCacheExpired(): Boolean {
        val fetchedAt = sharedPrefsManager.getExchangeRatesFetchedAtMillis()
        return fetchedAt <= 0L || System.currentTimeMillis() - fetchedAt > CACHE_TTL_MS
    }

    private fun fallbackRates(): Map<AppCurrency, Double> {
        return AppCurrency.entries.associateWith { it.fallbackVndPerUnit }
    }

    private companion object {
        const val API_URL = "https://open.er-api.com/v6/latest/VND"
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 8_000
        const val CACHE_TTL_MS = 12 * 60 * 60 * 1_000L
    }
}

package com.example.fap.utils

import android.content.Context
import android.util.Log
import com.example.fap.data.FapDatabase
import com.example.fap.data.entities.Currency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SharedCurrencyManager(context: Context) {

    private val availableCurrencies = hashMapOf(
        "A$" to "AUD",
        "BGN" to "BGN",
        "R$" to "BRL",
        "CA$" to "CAD",
        "CHF" to "CHF",
        "CN¥" to "CNY",
        "CZK" to "CZK",
        "DKK" to "DKK",
        "€" to "EUR",
        "£" to "GBP",
        "HK$" to "HKD",
        "HUF" to "HUF",
        "IDR" to "IDR",
        "₪" to "ILS",
        "₹" to "INR",
        "ISK" to "ISK",
        "JP¥" to "JPY",
        "₩" to "KRW",
        "MX$" to "MXN",
        "MYR" to "MYR",
        "NOK" to "NOK",
        "NZ$" to "NZD",
        "PHP" to "PHP",
        "PLN" to "PLN",
        "RON" to "RON",
        "SEK" to "SEK",
        "SGD" to "SGD",
        "THB" to "THB",
        "TRY" to "TRY",
        "US$" to "USD",
        "ZAR" to "ZAR",
    )
    private val sharedPreferences = SharedPreferencesManager.getInstance(context)
    private var defaultCurrency = sharedPreferences.getCurrency(context)

    fun num2Money(num: Number, currency: String = defaultCurrency): String {
        return "%.2f".format(num) + currency
    }

    private suspend fun updateCurrency(context: Context) {
        val url = URL("https://api.frankfurter.app/latest")
        GlobalScope.launch {
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000

                    val response = StringBuffer()
                    BufferedReader(InputStreamReader(inputStream)).use {
                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }
                    }

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try {
                            val jsonObject = JSONObject(response.toString())
                            val rates = jsonObject.getJSONObject("rates")

                            val db = FapDatabase.getInstance(context)
                            for (currency in rates.keys()) {
                                val newCurrency = availableCurrencies.filter { it.value == currency }.keys.first()
                                db.currencyDao().updateCurrency(
                                    Currency(
                                        newCurrency,
                                        rates.getDouble(currency)
                                    )
                                )
                            }
                            sharedPreferences.saveLastCurrencyUpdate(context)
                        } catch (e: Exception) {
                            Log.d("SharedCurrencyManager", "Couldn't parse json: $e")
                            return@launch
                        }
                    } else {
                        Log.d(
                            "SharedCurrencyManager",
                            "Couldn't update currency conversion: $responseCode"
                        )
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.d(
                        "SharedCurrencyManager",
                        "Couldn't connect to API: $e"
                    )
                    return@launch
                }
                return@launch
            }
        }
    }

    suspend fun tryUpdateCurrency(context: Context) {
        // only update once per day as the API also only updates once per day
        if (! sharedPreferences.isCurrencyUpToDate(context)) {
            updateCurrency(context)
        }
    }

    suspend fun initCurrency(context: Context) {
        // use default/old conversion if user doesn't have an Internet connection on first startup
        val availableCurrencyConversion = hashMapOf(
            "A$" to 1.6023,
            "BGN" to 1.9558,
            "R$" to 5.2965,
            "CA$" to 1.4362,
            "CHF" to 0.9716,
            "CN¥" to 7.6839,
            "CZK" to 23.666,
            "DKK" to 7.4505,
            "€" to 1.0,
            "£" to 0.85795,
            "HK$" to 8.4493,
            "HUF" to 368.73,
            "IDR" to 15997.0,
            "₪" to 3.8816,
            "₹" to 88.89,
            "ISK" to 149.5,
            "JP¥" to 150.24,
            "₩" to 1391.11,
            "MX$" to 18.7356,
            "MYR" to 4.9739,
            "NOK" to 11.612,
            "NZ$" to 1.7627,
            "PHP" to 60.426,
            "PLN" to 4.4605,
            "RON" to 4.9566,
            "SEK" to 11.673,
            "SGD" to 1.448,
            "THB" to 37.277,
            "TRY" to 25.124,
            "US$" to 1.078,
            "ZAR" to 20.181,
        )
        val db = FapDatabase.getInstance(context)
        var i = 0
        for (currency in availableCurrencyConversion.keys) {
            db.currencyDao().insertCurrency(Currency(currency, availableCurrencyConversion[currency]!!))
            i += 1
        }
        sharedPreferences.saveLastCurrencyUpdate(context, "0000-00-00")
        tryUpdateCurrency(context)
    }

    suspend fun calculateFromCurrency(amount: Double, currencyFrom: String, context: Context): Double {
        val db = FapDatabase.getInstance(context)
        val conversion = db.currencyDao().getConversion(defaultCurrency) / db.currencyDao().getConversion(currencyFrom)
        return amount * conversion
    }

    private suspend fun calculateToCurrency(currencyFrom: String, currencyTo: String, context: Context) {
        if (currencyFrom != currencyTo) {
            val db = FapDatabase.getInstance(context)
            val curUser = sharedPreferences.getCurUser(context)
            val conversion = db.currencyDao().getConversion(currencyTo) / db.currencyDao().getConversion(currencyFrom)
            var newPrice: Double

            val payments = db.fapDaoPayment().getPayments(curUser)
            val updatedPayments = payments.map { payment ->
                newPrice = payment.price * conversion
                payment.copy(price = newPrice)
            }
            db.fapDaoPayment().updatePayments(updatedPayments)
        }
    }

    fun updateDefaultCurrency(newCurrency: String, context: Context) {
        MainScope().launch {
            calculateToCurrency(defaultCurrency, newCurrency, context)
            defaultCurrency = newCurrency
        }
        sharedPreferences.saveCurrency(context, newCurrency)
    }

    fun getCurrency(): String {
        return defaultCurrency
    }

    fun getDefaultCurrencyIndex(): Int {
        return availableCurrencies.keys.indexOf(defaultCurrency)
    }

    fun getAvailableCurrencies(): MutableCollection<String> {
        return availableCurrencies.keys
    }

    companion object {
        private var instance: SharedCurrencyManager? = null

        fun getInstance(context: Context): SharedCurrencyManager {
            if (instance == null) {
                instance = SharedCurrencyManager(context.applicationContext)
            }
            return instance as SharedCurrencyManager
        }
    }
}
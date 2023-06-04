package com.example.fap.utils

import android.content.Context
import android.util.Log
import com.example.fap.R
import com.example.fap.data.FapDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency

class SharedCurrencyManager(context: Context) {

    private val availableCurrencies = arrayOf(
        "AUD",
        "BGN",
        "BRL",
        "CAD",
        "CHF",
        "CNY",
        "CZK",
        "DKK",
        "EUR",
        "GBP",
        "HKD",
        "HUF",
        "IDR",
        "ILS",
        "INR",
        "ISK",
        "JPY",
        "KRW",
        "MXN",
        "MYR",
        "NOK",
        "NZD",
        "PHP",
        "PLN",
        "RON",
        "SEK",
        "SGD",
        "THB",
        "TRY",
        "USD",
        "ZAR",
    )
    private val sharedPreferences = SharedPreferencesManager.getInstance(context)
    private val defaultCurrency = sharedPreferences.getString(context.getString(R.string.shared_prefs_current_currency), "EUR") ?: "EUR"

    fun num2Money(num: Number, currency: String = defaultCurrency): String {
        return "%.2f".format(num) + Currency.getInstance(currency).symbol
    }

    private suspend fun updateCurrency(context: Context): Boolean {
        val url = URL("https://api.frankfurter.app/latest")
        val errors = CompletableDeferred<Boolean>()
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
                            val date = jsonObject.getString("date")
                            val rates = jsonObject.getJSONObject("rates")

                            println("Date: $date") //TODO
                            val db = FapDatabase.getInstance(context)
                            for (currency in rates.keys()) {
                                db.currencyDao().upsertCurrency(
                                    com.example.fap.data.Currency(
                                        currency,
                                        rates.getDouble(currency)
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.d("SharedCurrencyManager", "Couldn't parse json: $e")
                            errors.complete(true)
                            return@launch
                        }
                    } else {
                        Log.d(
                            "SharedCurrencyManager",
                            "Couldn't update currency conversion: $responseCode"
                        )
                        errors.complete(true)
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.d(
                        "SharedCurrencyManager",
                        "Couldn't connect to API: $e"
                    )
                    errors.complete(true)
                    return@launch
                }
                errors.complete(false)
                return@launch
            }
        }
        return errors.await()
    }

    suspend fun tryUpdateCurrency(context: Context) {
        // only update once per day as the API also only updates once per day
        if (sharedPreferences.getLastCurrencyUpdate(context) != getDate()) {
            updateCurrency(context)
        }
    }

    suspend fun initCurrency(context: Context) {
        if (updateCurrency(context)) {
            // use default/old conversion if user doesn't have an Internet connection on first startup
            val availableCurrencyConversion = listOf<Double>(
                1.6248,
                1.9558,
                5.3752,
                1.4443,
                0.9758,
                7.6065,
                23.657,
                7.4488,
                1.00,
                0.8593,
                8.4346,
                371.36,
                15976.0,
                4.0311,
                88.64,
                150.1,
                149.46,
                1401.66,
                18.8527,
                4.9268,
                11.845,
                1.7652,
                60.132,
                4.4975,
                4.9622,
                11.5505,
                1.4474,
                37.251,
                22.474,
                1.0763,
                20.946,
            )
            val db = FapDatabase.getInstance(context)
            for (i in availableCurrencies.indices) {
                db.currencyDao().upsertCurrency(com.example.fap.data.Currency(availableCurrencies[i], availableCurrencyConversion[i]))
            }
            sharedPreferences.saveLastCurrencyUpdate(context, getDate())
        } else {
            sharedPreferences.saveLastCurrencyUpdate(context, "0000-00-00")
        }
    }

    suspend fun calculateFromCurrency(amount: Double, currencyFrom: String, context: Context): Double {
        // TODO use in AddPayment
        // TODO test
        val db = FapDatabase.getInstance(context)
        return if (currencyFrom != defaultCurrency) {
            val conversions = db.currencyDao().getConversion(currencyFrom, defaultCurrency)
            (amount / conversions[0]) * conversions[1]
        } else {
            amount
        }
    }

    suspend fun calculateToCurrency(amount: Double, currencyTo: String, context: Context){
        // TODO use in Settings (shouldn't create new db on every payment)
        // TODO test
        if (currencyTo != defaultCurrency) {
            val db = FapDatabase.getInstance(context)
            val curUser = sharedPreferences.getCurUser(context)
            val conversions = db.currencyDao().getConversion(defaultCurrency, currencyTo)
            val conversion = conversions[1] / conversions[0]
            var newPrice = 0.0

            val payments = db.fapDao().getPayments(curUser)
            /*
            for (payment in payments) {
                newPrice = payment.price * conversion
                db.fapDao().updatePayment(payment.copy(price = newPrice))
            }*/
            // TODO test this version too
            val updatedPayments = payments.map { payment ->
                newPrice = payment.price * conversion
                payment.copy(price = newPrice)
            }
            db.fapDao().updatePayments(updatedPayments)
        }
    }

    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
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
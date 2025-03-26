package com.example.cuco.util

import com.example.cuco.data.ExchangeRateApiService
import com.example.cuco.data.ExchangeRatesResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CurrencyParser {
    
    // Example: A simple regex for $49.99 or EUR 20.5
    private val priceRegex = Regex("([€$]|USD|EUR)\\s*([0-9]+(?:\\.[0-9]+)?)")
    
    // Your ExchangeRateApi base URL (example)
    private const val BASE_URL = "https://v6.exchangerate-api.com/v6/"
    private const val API_KEY = "feec8b7611be09b0cad59a2b" // put your real key here

    // Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ExchangeRateApiService::class.java)

    /**
     * Parse the recognized text, find all prices,
     * call exchange-rate API, then callback with final strings.
     */
    fun parsePrices(recognizedText: String, onResult: (List<String>) -> Unit) {
        // 1) Extract all prices
        val matches = priceRegex.findAll(recognizedText)
        val found = matches.map { match ->
            val symbol = match.groups[1]?.value ?: ""
            val amountStr = match.groups[2]?.value ?: ""
            PriceCandidate(symbol, amountStr.toDoubleOrNull() ?: 0.0)
        }.toList()

        if (found.isEmpty()) {
            onResult(emptyList())
            return
        }

        // 2) We guess the "from" currency by symbol
        //    If it's $, use "USD" as base, if "€", use "EUR" etc.
        //    If multiple different symbols found, you might handle each separately, or skip some.

        // Here we just handle the first price's symbol as the "base"
        val baseSymbol = found.first().symbol
        val baseCurrency = mapSymbolToCurrencyCode(baseSymbol) // e.g. "$" -> "USD", "€" -> "EUR"

        // 3) Call the ExchangeRate-API to get rates from that base currency
        //    We'll do it in a Coroutine for simplicity (the UI won't block).
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService
                    .getLatestRates(
                        base = baseCurrency,
                        apiKey = API_KEY
                    )
                    .execute()

                if (response.isSuccessful) {
                    val body: ExchangeRatesResponse? = response.body()
                    val rates = body?.conversionRates
                    if (rates != null) {
                        // 4) For each found price, convert to HUF (or whatever you want)
                        val results = found.map { price ->
                            // if baseSymbol not the same as price symbol, you might handle differently
                            val convertedHuf = convertPrice(rates, price.amount, "HUF")
                            "${price.symbol} ${price.amount} -> $convertedHuf Ft"
                        }
                        // callback
                        onResult(results)
                    } else {
                        onResult(emptyList())
                    }
                } else {
                    onResult(emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(emptyList())
            }
        }
    }

    // A simple helper to pick the currency code from symbol
    private fun mapSymbolToCurrencyCode(symbol: String): String {
        return when (symbol) {
            "$", "USD" -> "USD"
            "€", "EUR" -> "EUR"
            else -> "USD" // default
        }
    }

    /**
     * Convert a given 'amount' of the base currency to the 'targetCurrency' using the rates map.
     */
    private fun convertPrice(rates: Map<String, Double>, amount: Double, targetCurrency: String): Double {
        // e.g. if base = USD, then rates["HUF"] might be 367.0
        val rate = rates[targetCurrency] ?: 1.0
        return amount * rate
    }

    // Just hold a discovered price
    data class PriceCandidate(val symbol: String, val amount: Double)
}

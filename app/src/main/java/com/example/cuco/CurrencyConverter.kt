package com.example.cuco

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object CurrencyConverter {

    private const val API_URL = "https://v6.exchangerate-api.com/v6/YOUR_API_KEY/latest/"

    interface CurrencyApi {
        @GET("USD")
        suspend fun getRates(): CurrencyResponse
    }

    private val api = Retrofit.Builder()
        .baseUrl(API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CurrencyApi::class.java)

    fun convertCurrency(amount: String, onResult: (String) -> Unit) {
        Thread {
            try {
                val response = api.getRates()
                val rate = response.rates["EUR"] ?: 1.0
                val convertedValue = amount.toDouble() * rate
                onResult("%.2f EUR".format(convertedValue))
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }.start()
    }
}

package com.example.currencylensapp.util

object CurrencyParser {

    // Egyszerű regex: pl. "$49.99" vagy "EUR 20.5"
    private val priceRegex = Regex("([€$]|USD|EUR)\\s*([0-9]+(?:\\.[0-9]+)?)")

    fun findPrices(fullText: String): List<String> {
        val results = mutableListOf<String>()
        val matches = priceRegex.findAll(fullText)

        for (match in matches) {
            val currencySymbol = match.groups[1]?.value ?: ""
            val amountString = match.groups[2]?.value ?: ""
            val amount = amountString.toDoubleOrNull() ?: 0.0

            // Fix konverzió (ha valós API is kell, azt itt cserélheted)
            val converted = convertToHUF(currencySymbol, amount)

            val displayString = "$currencySymbol $amount -> $converted Ft"
            results.add(displayString)
        }
        return results
    }

    private fun convertToHUF(symbol: String, amount: Double): Double {
        // Példa fix értékek:
        return when (symbol) {
            "$", "USD" -> amount * 365
            "€", "EUR" -> amount * 400
            else -> amount // pl. ha HUF vagy ismeretlen, nem váltjuk
        }
    }
}

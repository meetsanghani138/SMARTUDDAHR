package com.marwadiuniversity.radhe.utils

import com.marwadiuniversity.radhe.models.Product

data class ParsedVoiceItem(
    val product: Product,
    var quantity: Int
)

object VoicePurchaseParser {

    private val numberMap = mapOf(
        "1" to 1, "2" to 2, "3" to 3, "4" to 4, "5" to 5, "6" to 6, "7" to 7, "8" to 8, "9" to 9, "10" to 10,
        "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
        "એક" to 1, "બે" to 2, "ત્રણ" to 3, "ચાર" to 4, "પાંચ" to 5, "છ" to 6, "સાત" to 7, "આઠ" to 8, "નવ" to 9, "દસ" to 10,
        "एक" to 1, "दो" to 2, "तीन" to 3, "चार" to 4, "पांच" to 5, "छह" to 6, "सात" to 7, "आठ" to 8, "नौ" to 9, "दस" to 10
    )

    fun parseSpokenText(spokenText: String, availableProducts: List<Product>): List<ParsedVoiceItem> {
        val results = mutableListOf<ParsedVoiceItem>()
        val lowerText = spokenText.lowercase().trim()

        for (product in availableProducts) {
            val pName = product.name.lowercase().trim()
            val matches = lowerText.contains(pName) ||
                    pName.split(" ").any { it.length > 2 && lowerText.contains(it) } ||
                    (pName.contains("vimal") && (lowerText.contains("વિમલ") || lowerText.contains("विमल"))) ||
                    (pName.contains("cold") && (lowerText.contains("કોલ્ડ") || lowerText.contains("कोल्ड"))) ||
                    (pName.contains("pan") && (lowerText.contains("પાન") || lowerText.contains("पान")))

            if (matches) {
                var qty = 1
                val words = lowerText.split(" ", ",", "અને", "और", "and")
                for (word in words) {
                    val cleanWord = word.trim()
                    if (numberMap.containsKey(cleanWord)) {
                        qty = numberMap[cleanWord]!!
                        break
                    } else if (cleanWord.toIntOrNull() != null) {
                        qty = cleanWord.toInt()
                        break
                    }
                }
                results.add(ParsedVoiceItem(product, qty))
            }
        }
        return results
    }
}

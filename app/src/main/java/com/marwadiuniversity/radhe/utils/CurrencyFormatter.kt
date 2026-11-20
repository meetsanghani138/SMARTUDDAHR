package com.marwadiuniversity.radhe.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()

    fun formatPaiseToRupees(paise: Long): String {
        val rupees = paise / 100.0
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(rupees).replace("INR", "₹")
    }

    fun formatRupeesDouble(rupees: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(rupees).replace("INR", "₹")
    }

    fun parseRupeesToPaise(rupeesStr: String): Long {
        if (rupeesStr.isBlank()) return 0L
        val clean = rupeesStr.replace("₹", "").replace(",", "").trim()
        val amount = clean.toDoubleOrNull() ?: 0.0
        return Math.round(amount * 100.0)
    }
}

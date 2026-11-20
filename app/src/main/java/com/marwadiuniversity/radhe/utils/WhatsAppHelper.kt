package com.marwadiuniversity.radhe.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.marwadiuniversity.radhe.database.DatabaseHelper
import java.net.URLEncoder

object WhatsAppHelper {

    fun formatMessage(
        language: String,
        customerName: String,
        shopName: String,
        amountRupeesStr: String
    ): String {
        return when (language.lowercase()) {
            "gu", "gujarati" -> "નમસ્તે $customerName, તમારી પાસે $shopName નુ $amountRupeesStr ઉધાર બાકી છે. કૃપા કરીને અનુકૂળ સમયે ચુકવણી કરશો. આભાર!"
            "hi", "hindi" -> "नमस्ते $customerName, आपकी $shopName पर $amountRupeesStr बकाया है। कृपया सुविधानुसार भुगतान करें। धन्यवाद!"
            else -> "Hello $customerName, your outstanding balance at $shopName is $amountRupeesStr. Please make the payment at your convenience. Thank you!"
        }
    }

    fun sendWhatsAppReminder(
        context: Context,
        customerId: Long,
        customerName: String,
        customerMobile: String,
        shopName: String,
        outstandingPaise: Long,
        language: String = "en"
    ): Boolean {
        val amountStr = CurrencyFormatter.formatPaiseToRupees(outstandingPaise)
        val message = formatMessage(language, customerName, shopName, amountStr)

        val cleanPhone = customerMobile.replace("+", "").replace(" ", "").trim()
        val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone

        val encodedMsg = URLEncoder.encode(message, "UTF-8")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMsg")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)

            // Log reminder in SQLite
            val db = DatabaseHelper.getInstance(context).writableDatabase
            val cv = ContentValues().apply {
                put("customer_id", customerId)
                put("customer_name", customerName)
                put("customer_mobile", customerMobile)
                put("message", message)
                put("outstanding_amount_paise", outstandingPaise)
                put("sent_at", DateTimeHelper.getCurrentIsoTimestamp())
                put("language", language)
                put("channel", "WHATSAPP")
            }
            db.insert("reminders", null, cv)
            true
        } catch (e: Exception) {
            false
        }
    }
}

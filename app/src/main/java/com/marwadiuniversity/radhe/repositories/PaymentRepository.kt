package com.marwadiuniversity.radhe.repositories

import android.content.ContentValues
import android.content.Context
import com.marwadiuniversity.radhe.database.DatabaseHelper
import com.marwadiuniversity.radhe.models.Payment
import com.marwadiuniversity.radhe.utils.DateTimeHelper
import com.marwadiuniversity.radhe.utils.getLongByColumn
import com.marwadiuniversity.radhe.utils.getStringByColumn
import java.util.UUID

class PaymentRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val customerRepo = CustomerRepository(context)

    fun recordPayment(
        customerId: Long,
        amountPaise: Long,
        paymentMethod: String,
        referenceNumber: String = "",
        notes: String = "",
        createdBy: String = "Owner",
        userId: Long = 1L
    ): Payment? {
        if (amountPaise <= 0) return null

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val customer = customerRepo.getCustomerById(customerId, userId) ?: return null
            val previousBalance = customer.currentOutstandingPaise
            val newBalancePaise = (previousBalance - amountPaise).coerceAtLeast(0L)

            val timestamp = DateTimeHelper.getCurrentIsoTimestamp()
            val txnNumber = "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().take(4).uppercase()

            val cvTxn = ContentValues().apply {
                put("user_id", userId)
                put("transaction_number", txnNumber)
                put("customer_id", customerId)
                put("type", "PAYMENT_RECEIVED")
                put("total_amount_paise", amountPaise)
                put("previous_balance_paise", previousBalance)
                put("new_balance_paise", newBalancePaise)
                put("created_by", createdBy)
                put("created_at", timestamp)
                put("notes", notes)
                put("is_reversed", 0)
            }
            val txnId = db.insert("transactions", null, cvTxn)
            if (txnId <= 0) return null

            val cvPay = ContentValues().apply {
                put("user_id", userId)
                put("transaction_id", txnId)
                put("customer_id", customerId)
                put("amount_paise", amountPaise)
                put("payment_method", paymentMethod)
                put("reference_number", referenceNumber)
                put("notes", notes)
                put("created_at", timestamp)
            }
            val payId = db.insert("payments", null, cvPay)

            customerRepo.updateOutstanding(db, customerId, newBalancePaise, userId)

            db.setTransactionSuccessful()

            return Payment(
                id = payId,
                transactionId = txnId,
                customerId = customerId,
                customerName = customer.name,
                amountPaise = amountPaise,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                notes = notes,
                createdAt = timestamp
            )
        } finally {
            db.endTransaction()
        }
    }

    fun getAllPayments(customerId: Long = 0, userId: Long = 1L): List<Payment> {
        val list = mutableListOf<Payment>()
        val db = dbHelper.readableDatabase

        var sql = """
            SELECT p.*, c.name as customer_name 
            FROM payments p 
            LEFT JOIN customers c ON p.customer_id = c.id 
            WHERE p.user_id = ?
        """.trimIndent()

        val args = mutableListOf(userId.toString())
        if (customerId > 0) {
            sql += " AND p.customer_id = ?"
            args.add(customerId.toString())
        }
        sql += " ORDER BY p.id DESC"

        val cursor = db.rawQuery(sql, args.toTypedArray())
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Payment(
                        id = c.getLongByColumn("id"),
                        transactionId = c.getLongByColumn("transaction_id"),
                        customerId = c.getLongByColumn("customer_id"),
                        customerName = c.getStringByColumn("customer_name"),
                        amountPaise = c.getLongByColumn("amount_paise"),
                        paymentMethod = c.getStringByColumn("payment_method"),
                        referenceNumber = c.getStringByColumn("reference_number"),
                        notes = c.getStringByColumn("notes"),
                        createdAt = c.getStringByColumn("created_at")
                    )
                )
            }
        }
        return list
    }
}

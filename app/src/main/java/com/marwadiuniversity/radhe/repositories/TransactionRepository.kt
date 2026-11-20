package com.marwadiuniversity.radhe.repositories

import android.content.ContentValues
import android.content.Context
import com.marwadiuniversity.radhe.database.DatabaseHelper
import com.marwadiuniversity.radhe.models.TransactionItem
import com.marwadiuniversity.radhe.models.TransactionRecord
import com.marwadiuniversity.radhe.utils.DateTimeHelper
import com.marwadiuniversity.radhe.utils.getBooleanByColumn
import com.marwadiuniversity.radhe.utils.getIntByColumn
import com.marwadiuniversity.radhe.utils.getLongByColumn
import com.marwadiuniversity.radhe.utils.getStringByColumn
import java.util.UUID

class TransactionRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val customerRepo = CustomerRepository(context)

    fun createUdharTransaction(
        customerId: Long,
        items: List<TransactionItem>,
        createdBy: String = "Owner",
        notes: String = "",
        userId: Long = 1L
    ): TransactionRecord? {
        val validItems = items.filter { it.quantity > 0 }
        if (validItems.isEmpty()) return null

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val customer = customerRepo.getCustomerById(customerId, userId) ?: return null
            val previousBalance = customer.currentOutstandingPaise

            val totalAmountPaise = validItems.sumOf { it.totalPricePaise }
            val newBalancePaise = previousBalance + totalAmountPaise

            val txnNumber = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().take(4).uppercase()
            val timestamp = DateTimeHelper.getCurrentIsoTimestamp()

            val cvTxn = ContentValues().apply {
                put("user_id", userId)
                put("transaction_number", txnNumber)
                put("customer_id", customerId)
                put("type", "CREDIT_PURCHASE")
                put("total_amount_paise", totalAmountPaise)
                put("previous_balance_paise", previousBalance)
                put("new_balance_paise", newBalancePaise)
                put("created_by", createdBy)
                put("created_at", timestamp)
                put("notes", notes)
                put("is_reversed", 0)
            }
            val txnId = db.insert("transactions", null, cvTxn)
            if (txnId <= 0) return null

            val insertedItems = mutableListOf<TransactionItem>()
            for (item in validItems) {
                val cvItem = ContentValues().apply {
                    put("transaction_id", txnId)
                    put("product_id", item.productId)
                    put("product_name", item.productName)
                    put("quantity", item.quantity)
                    put("unit_price_paise", item.unitPricePaise)
                    put("total_price_paise", item.totalPricePaise)
                }
                val itemId = db.insert("transaction_items", null, cvItem)

                if (item.productId > 0) {
                    db.execSQL(
                        "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND user_id = ?",
                        arrayOf<Any>(item.quantity, item.productId, userId)
                    )
                }

                insertedItems.add(item.copy(id = itemId, transactionId = txnId))
            }

            customerRepo.updateOutstanding(db, customerId, newBalancePaise, userId)

            db.setTransactionSuccessful()

            return TransactionRecord(
                id = txnId,
                transactionNumber = txnNumber,
                customerId = customerId,
                customerName = customer.name,
                customerMobile = customer.mobile,
                type = "CREDIT_PURCHASE",
                totalAmountPaise = totalAmountPaise,
                previousBalancePaise = previousBalance,
                newBalancePaise = newBalancePaise,
                createdBy = createdBy,
                createdAt = timestamp,
                notes = notes,
                isReversed = false,
                items = insertedItems
            )
        } finally {
            db.endTransaction()
        }
    }

    fun getAllTransactions(
        query: String = "",
        typeFilter: String = "ALL",
        customerId: Long = 0,
        userId: Long = 1L
    ): List<TransactionRecord> {
        val list = mutableListOf<TransactionRecord>()
        val db = dbHelper.readableDatabase

        var sql = """
            SELECT t.*, c.name as customer_name, c.mobile as customer_mobile 
            FROM transactions t 
            LEFT JOIN customers c ON t.customer_id = c.id 
            WHERE t.user_id = ?
        """.trimIndent()

        val args = mutableListOf(userId.toString())

        if (customerId > 0) {
            sql += " AND t.customer_id = ?"
            args.add(customerId.toString())
        }

        if (typeFilter != "ALL") {
            sql += " AND t.type = ?"
            args.add(typeFilter)
        }

        if (query.isNotBlank()) {
            sql += " AND (c.name LIKE ? OR t.transaction_number LIKE ?)"
            val q = "%$query%"
            args.add(q)
            args.add(q)
        }

        sql += " ORDER BY t.id DESC"

        val cursor = db.rawQuery(sql, args.toTypedArray())
        cursor.use { c ->
            while (c.moveToNext()) {
                val txnId = c.getLongByColumn("id")
                val items = getItemsForTransaction(txnId)
                list.add(
                    TransactionRecord(
                        id = txnId,
                        transactionNumber = c.getStringByColumn("transaction_number"),
                        customerId = c.getLongByColumn("customer_id"),
                        customerName = c.getStringByColumn("customer_name"),
                        customerMobile = c.getStringByColumn("customer_mobile"),
                        type = c.getStringByColumn("type"),
                        totalAmountPaise = c.getLongByColumn("total_amount_paise"),
                        previousBalancePaise = c.getLongByColumn("previous_balance_paise"),
                        newBalancePaise = c.getLongByColumn("new_balance_paise"),
                        createdBy = c.getStringByColumn("created_by"),
                        createdAt = c.getStringByColumn("created_at"),
                        notes = c.getStringByColumn("notes"),
                        isReversed = c.getBooleanByColumn("is_reversed"),
                        items = items
                    )
                )
            }
        }
        return list
    }

    fun getTransactionById(txnId: Long, userId: Long = 1L): TransactionRecord? {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT t.*, c.name as customer_name, c.mobile as customer_mobile 
            FROM transactions t 
            LEFT JOIN customers c ON t.customer_id = c.id 
            WHERE t.id = ? AND t.user_id = ?
        """.trimIndent()
        val cursor = db.rawQuery(sql, arrayOf(txnId.toString(), userId.toString()))
        cursor.use { c ->
            if (c.moveToFirst()) {
                val items = getItemsForTransaction(txnId)
                return TransactionRecord(
                    id = txnId,
                    transactionNumber = c.getStringByColumn("transaction_number"),
                    customerId = c.getLongByColumn("customer_id"),
                    customerName = c.getStringByColumn("customer_name"),
                    customerMobile = c.getStringByColumn("customer_mobile"),
                    type = c.getStringByColumn("type"),
                    totalAmountPaise = c.getLongByColumn("total_amount_paise"),
                    previousBalancePaise = c.getLongByColumn("previous_balance_paise"),
                    newBalancePaise = c.getLongByColumn("new_balance_paise"),
                    createdBy = c.getStringByColumn("created_by"),
                    createdAt = c.getStringByColumn("created_at"),
                    notes = c.getStringByColumn("notes"),
                    isReversed = c.getBooleanByColumn("is_reversed"),
                    items = items
                )
            }
        }
        return null
    }

    private fun getItemsForTransaction(txnId: Long): List<TransactionItem> {
        val items = mutableListOf<TransactionItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM transaction_items WHERE transaction_id = ?", arrayOf(txnId.toString()))
        cursor.use { c ->
            while (c.moveToNext()) {
                items.add(
                    TransactionItem(
                        id = c.getLongByColumn("id"),
                        transactionId = txnId,
                        productId = c.getLongByColumn("product_id"),
                        productName = c.getStringByColumn("product_name"),
                        quantity = c.getIntByColumn("quantity"),
                        unitPricePaise = c.getLongByColumn("unit_price_paise"),
                        totalPricePaise = c.getLongByColumn("total_price_paise")
                    )
                )
            }
        }
        return items
    }

    fun reverseTransaction(txnId: Long, reason: String = "Reversal", userId: Long = 1L): Boolean {
        val txn = getTransactionById(txnId, userId) ?: return false
        if (txn.isReversed) return false

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            if (txn.type == "CREDIT_PURCHASE") {
                for (item in txn.items) {
                    if (item.productId > 0) {
                        db.execSQL(
                            "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ? AND user_id = ?",
                            arrayOf<Any>(item.quantity, item.productId, userId)
                        )
                    }
                }
            }

            val cvTxn = ContentValues().apply {
                put("is_reversed", 1)
                put("notes", "${txn.notes} (Reversed: $reason)")
            }
            db.update("transactions", cvTxn, "id = ? AND user_id = ?", arrayOf(txnId.toString(), userId.toString()))

            customerRepo.reconcileCustomerBalance(txn.customerId, userId)

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }
}

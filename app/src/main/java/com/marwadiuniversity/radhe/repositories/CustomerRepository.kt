package com.marwadiuniversity.radhe.repositories

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.marwadiuniversity.radhe.database.DatabaseHelper
import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.utils.DateTimeHelper
import com.marwadiuniversity.radhe.utils.getBooleanByColumn
import com.marwadiuniversity.radhe.utils.getLongByColumn
import com.marwadiuniversity.radhe.utils.getStringByColumn

class CustomerRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun getAllCustomers(query: String = "", showArchived: Boolean = false, userId: Long = 1L): List<Customer> {
        val list = mutableListOf<Customer>()
        val db = dbHelper.readableDatabase

        var selection = if (showArchived) "user_id = ?" else "user_id = ? AND is_archived = 0"
        val selectionArgs = mutableListOf(userId.toString())

        if (query.isNotBlank()) {
            selection += " AND (name LIKE ? OR mobile LIKE ?)"
            val q = "%$query%"
            selectionArgs.add(q)
            selectionArgs.add(q)
        }

        val cursor = db.query(
            "customers",
            null,
            selection,
            selectionArgs.toTypedArray(),
            null,
            null,
            "name ASC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Customer(
                        id = c.getLongByColumn("id"),
                        name = c.getStringByColumn("name"),
                        mobile = c.getStringByColumn("mobile"),
                        address = c.getStringByColumn("address"),
                        openingBalancePaise = c.getLongByColumn("opening_balance_paise"),
                        currentOutstandingPaise = c.getLongByColumn("current_outstanding_paise"),
                        notes = c.getStringByColumn("notes"),
                        isArchived = c.getBooleanByColumn("is_archived"),
                        createdAt = c.getStringByColumn("created_at")
                    )
                )
            }
        }
        return list
    }

    fun getCustomerById(id: Long, userId: Long = 1L): Customer? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("customers", null, "id = ? AND user_id = ?", arrayOf(id.toString(), userId.toString()), null, null, null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                return Customer(
                    id = c.getLongByColumn("id"),
                    name = c.getStringByColumn("name"),
                    mobile = c.getStringByColumn("mobile"),
                    address = c.getStringByColumn("address"),
                    openingBalancePaise = c.getLongByColumn("opening_balance_paise"),
                    currentOutstandingPaise = c.getLongByColumn("current_outstanding_paise"),
                    notes = c.getStringByColumn("notes"),
                    isArchived = c.getBooleanByColumn("is_archived"),
                    createdAt = c.getStringByColumn("created_at")
                )
            }
        }
        return null
    }

    fun addCustomer(customer: Customer, userId: Long = 1L): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("name", customer.name)
            put("mobile", customer.mobile)
            put("address", customer.address)
            put("opening_balance_paise", customer.openingBalancePaise)
            put("current_outstanding_paise", customer.openingBalancePaise)
            put("notes", customer.notes)
            put("is_archived", if (customer.isArchived) 1 else 0)
            put("created_at", customer.createdAt.ifBlank { DateTimeHelper.getCurrentIsoTimestamp() })
        }
        return db.insert("customers", null, cv)
    }

    fun updateCustomer(customer: Customer, userId: Long = 1L): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", customer.name)
            put("mobile", customer.mobile)
            put("address", customer.address)
            put("notes", customer.notes)
        }
        return db.update("customers", cv, "id = ? AND user_id = ?", arrayOf(customer.id.toString(), userId.toString())) > 0
    }

    fun archiveCustomer(id: Long, userId: Long = 1L): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("is_archived", 1)
        }
        return db.update("customers", cv, "id = ? AND user_id = ?", arrayOf(id.toString(), userId.toString())) > 0
    }

    fun updateOutstanding(db: SQLiteDatabase, customerId: Long, newBalancePaise: Long, userId: Long = 1L) {
        val cv = ContentValues().apply {
            put("current_outstanding_paise", newBalancePaise)
        }
        db.update("customers", cv, "id = ? AND user_id = ?", arrayOf(customerId.toString(), userId.toString()))
    }

    fun reconcileCustomerBalance(customerId: Long, userId: Long = 1L): Long {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            var openingBalance = 0L
            val cCust = db.rawQuery("SELECT opening_balance_paise FROM customers WHERE id = ? AND user_id = ?", arrayOf(customerId.toString(), userId.toString()))
            cCust.use { c ->
                if (c.moveToFirst()) {
                    openingBalance = c.getLongByColumn("opening_balance_paise")
                }
            }

            var totalUdhar = 0L
            val cUdhar = db.rawQuery(
                "SELECT SUM(total_amount_paise) as total FROM transactions WHERE customer_id = ? AND user_id = ? AND type = 'CREDIT_PURCHASE' AND is_reversed = 0",
                arrayOf(customerId.toString(), userId.toString())
            )
            cUdhar.use { c ->
                if (c.moveToFirst()) {
                    totalUdhar = c.getLongByColumn("total")
                }
            }

            var totalPayments = 0L
            val cPay = db.rawQuery(
                "SELECT SUM(amount_paise) as total FROM payments WHERE customer_id = ? AND user_id = ?",
                arrayOf(customerId.toString(), userId.toString())
            )
            cPay.use { c ->
                if (c.moveToFirst()) {
                    totalPayments = c.getLongByColumn("total")
                }
            }

            val recalculatedOutstanding = openingBalance + totalUdhar - totalPayments
            updateOutstanding(db, customerId, recalculatedOutstanding, userId)
            db.setTransactionSuccessful()
            return recalculatedOutstanding
        } finally {
            db.endTransaction()
        }
    }
}

package com.marwadiuniversity.radhe.utils

import android.content.ContentValues
import android.content.Context
import com.marwadiuniversity.radhe.database.DatabaseHelper
import com.marwadiuniversity.radhe.repositories.CustomerRepository
import com.marwadiuniversity.radhe.repositories.ProductRepository
import com.marwadiuniversity.radhe.repositories.TransactionRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupRestoreManager {

    fun createBackupJson(context: Context): File? {
        val dbHelper = DatabaseHelper.getInstance(context)
        val customerRepo = CustomerRepository(context)
        val productRepo = ProductRepository(context)
        val txnRepo = TransactionRepository(context)

        val rootJson = JSONObject()
        rootJson.put("version", DatabaseHelper.DATABASE_VERSION)
        rootJson.put("timestamp", DateTimeHelper.getCurrentIsoTimestamp())

        // Customers
        val customersArray = JSONArray()
        for (c in customerRepo.getAllCustomers(showArchived = true)) {
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("mobile", c.mobile)
                put("address", c.address)
                put("openingBalancePaise", c.openingBalancePaise)
                put("currentOutstandingPaise", c.currentOutstandingPaise)
                put("notes", c.notes)
                put("isArchived", c.isArchived)
                put("createdAt", c.createdAt)
            }
            customersArray.put(cObj)
        }
        rootJson.put("customers", customersArray)

        // Products
        val productsArray = JSONArray()
        for (p in productRepo.getAllProducts()) {
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("sku", p.sku)
                put("name", p.name)
                put("categoryId", p.categoryId)
                put("purchasePricePaise", p.purchasePricePaise)
                put("sellingPricePaise", p.sellingPricePaise)
                put("stockQuantity", p.stockQuantity)
                put("unit", p.unit)
                put("isActive", p.isActive)
                put("createdAt", p.createdAt)
            }
            productsArray.put(pObj)
        }
        rootJson.put("products", productsArray)

        // Transactions
        val txnsArray = JSONArray()
        for (t in txnRepo.getAllTransactions()) {
            val tObj = JSONObject().apply {
                put("id", t.id)
                put("transactionNumber", t.transactionNumber)
                put("customerId", t.customerId)
                put("type", t.type)
                put("totalAmountPaise", t.totalAmountPaise)
                put("previousBalancePaise", t.previousBalancePaise)
                put("newBalancePaise", t.newBalancePaise)
                put("createdBy", t.createdBy)
                put("createdAt", t.createdAt)
                put("notes", t.notes)
                put("isReversed", t.isReversed)
            }
            txnsArray.put(tObj)
        }
        rootJson.put("transactions", txnsArray)

        val file = File(context.cacheDir, "smart_udhar_backup_${System.currentTimeMillis()}.json")
        return try {
            val fos = FileOutputStream(file)
            fos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            fos.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun restoreFromJsonString(context: Context, jsonStr: String): Boolean {
        val dbHelper = DatabaseHelper.getInstance(context)
        val db = dbHelper.writableDatabase

        return try {
            val root = JSONObject(jsonStr)
            db.beginTransaction()

            if (root.has("customers")) {
                val custArray = root.getJSONArray("customers")
                for (i in 0 until custArray.length()) {
                    val c = custArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("name", c.optString("name"))
                        put("mobile", c.optString("mobile"))
                        put("address", c.optString("address"))
                        put("opening_balance_paise", c.optLong("openingBalancePaise"))
                        put("current_outstanding_paise", c.optLong("currentOutstandingPaise"))
                        put("notes", c.optString("notes"))
                        put("is_archived", if (c.optBoolean("isArchived")) 1 else 0)
                        put("created_at", c.optString("createdAt", DateTimeHelper.getCurrentIsoTimestamp()))
                    }
                    db.insertWithOnConflict("customers", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                }
            }

            db.setTransactionSuccessful()
            db.endTransaction()
            true
        } catch (e: Exception) {
            try { db.endTransaction() } catch (_: Exception) {}
            false
        }
    }
}

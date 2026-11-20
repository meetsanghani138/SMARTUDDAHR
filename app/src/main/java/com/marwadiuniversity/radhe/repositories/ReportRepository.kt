package com.marwadiuniversity.radhe.repositories

import android.content.Context
import com.marwadiuniversity.radhe.database.DatabaseHelper
import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.models.Product
import com.marwadiuniversity.radhe.utils.DateTimeHelper
import com.marwadiuniversity.radhe.utils.getBooleanByColumn
import com.marwadiuniversity.radhe.utils.getIntByColumn
import com.marwadiuniversity.radhe.utils.getLongByColumn
import com.marwadiuniversity.radhe.utils.getStringByColumn

data class DashboardSummary(
    val todaysSalesPaise: Long,
    val totalUdharPaise: Long,
    val paymentsReceivedPaise: Long,
    val totalCustomers: Int,
    val totalProducts: Int,
    val lowStockCount: Int
)

data class SalesDataPoint(
    val label: String,
    val amountPaise: Long
)

class ReportRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun getDashboardSummary(userId: Long = 1L): DashboardSummary {
        val db = dbHelper.readableDatabase
        val todayStr = DateTimeHelper.getCurrentDateIso()

        var todaysSales = 0L
        val cToday = db.rawQuery(
            "SELECT SUM(total_amount_paise) as total FROM transactions WHERE user_id = ? AND type = 'CREDIT_PURCHASE' AND is_reversed = 0 AND created_at LIKE ?",
            arrayOf(userId.toString(), "$todayStr%")
        )
        cToday.use { c ->
            if (c.moveToFirst()) {
                todaysSales = c.getLongByColumn("total")
            }
        }

        var totalUdhar = 0L
        val cUdhar = db.rawQuery(
            "SELECT SUM(current_outstanding_paise) as total FROM customers WHERE user_id = ? AND is_archived = 0",
            arrayOf(userId.toString())
        )
        cUdhar.use { c ->
            if (c.moveToFirst()) {
                totalUdhar = c.getLongByColumn("total")
            }
        }

        var paymentsReceived = 0L
        val cPay = db.rawQuery(
            "SELECT SUM(amount_paise) as total FROM payments WHERE user_id = ? AND created_at LIKE ?",
            arrayOf(userId.toString(), "$todayStr%")
        )
        cPay.use { c ->
            if (c.moveToFirst()) {
                paymentsReceived = c.getLongByColumn("total")
            }
        }

        var totalCust = 0
        val cCustCount = db.rawQuery("SELECT COUNT(*) as count FROM customers WHERE user_id = ? AND is_archived = 0", arrayOf(userId.toString()))
        cCustCount.use { c ->
            if (c.moveToFirst()) {
                totalCust = c.getIntByColumn("count")
            }
        }

        var totalProd = 0
        val cProdCount = db.rawQuery("SELECT COUNT(*) as count FROM products WHERE user_id = ? AND is_active = 1", arrayOf(userId.toString()))
        cProdCount.use { c ->
            if (c.moveToFirst()) {
                totalProd = c.getIntByColumn("count")
            }
        }

        var lowStock = 0
        val cLow = db.rawQuery("SELECT COUNT(*) as count FROM products WHERE user_id = ? AND is_active = 1 AND stock_quantity <= 10", arrayOf(userId.toString()))
        cLow.use { c ->
            if (c.moveToFirst()) {
                lowStock = c.getIntByColumn("count")
            }
        }

        return DashboardSummary(
            todaysSalesPaise = todaysSales,
            totalUdharPaise = totalUdhar,
            paymentsReceivedPaise = paymentsReceived,
            totalCustomers = totalCust,
            totalProducts = totalProd,
            lowStockCount = lowStock
        )
    }

    fun getLowStockProducts(threshold: Int = 10, userId: Long = 1L): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT p.*, c.name as category_name 
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            WHERE p.user_id = ? AND p.is_active = 1 AND p.stock_quantity <= ? 
            ORDER BY p.stock_quantity ASC
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(userId.toString(), threshold.toString()))
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Product(
                        id = c.getLongByColumn("id"),
                        sku = c.getStringByColumn("sku"),
                        name = c.getStringByColumn("name"),
                        categoryId = c.getLongByColumn("category_id"),
                        categoryName = c.getStringByColumn("category_name"),
                        purchasePricePaise = c.getLongByColumn("purchase_price_paise"),
                        sellingPricePaise = c.getLongByColumn("selling_price_paise"),
                        stockQuantity = c.getIntByColumn("stock_quantity"),
                        unit = c.getStringByColumn("unit"),
                        imagePath = c.getStringByColumn("image_path"),
                        isActive = c.getBooleanByColumn("is_active"),
                        createdAt = c.getStringByColumn("created_at")
                    )
                )
            }
        }
        return list
    }

    fun getTopOutstandingCustomers(limit: Int = 5, userId: Long = 1L): List<Customer> {
        val list = mutableListOf<Customer>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM customers WHERE user_id = ? AND is_archived = 0 AND current_outstanding_paise > 0 ORDER BY current_outstanding_paise DESC LIMIT ?",
            arrayOf(userId.toString(), limit.toString())
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

    fun getDailySalesData(userId: Long = 1L): List<SalesDataPoint> {
        val list = mutableListOf<SalesDataPoint>()
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT SUBSTR(created_at, 1, 10) as sale_date, SUM(total_amount_paise) as total 
            FROM transactions 
            WHERE user_id = ? AND type = 'CREDIT_PURCHASE' AND is_reversed = 0 
            GROUP BY sale_date 
            ORDER BY sale_date DESC 
            LIMIT 7
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(userId.toString()))
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    SalesDataPoint(
                        label = c.getStringByColumn("sale_date"),
                        amountPaise = c.getLongByColumn("total")
                    )
                )
            }
        }
        return list.reversed()
    }
}

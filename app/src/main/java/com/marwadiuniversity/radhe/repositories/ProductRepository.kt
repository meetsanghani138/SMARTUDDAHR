package com.marwadiuniversity.radhe.repositories

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.marwadiuniversity.radhe.database.DatabaseHelper
import com.marwadiuniversity.radhe.models.Category
import com.marwadiuniversity.radhe.models.Product
import com.marwadiuniversity.radhe.utils.DateTimeHelper
import com.marwadiuniversity.radhe.utils.getBooleanByColumn
import com.marwadiuniversity.radhe.utils.getIntByColumn
import com.marwadiuniversity.radhe.utils.getLongByColumn
import com.marwadiuniversity.radhe.utils.getStringByColumn

class ProductRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun getAllProducts(
        query: String = "",
        categoryId: Long = 0,
        sortBy: String = "NAME",
        userId: Long = 1L
    ): List<Product> {
        val list = mutableListOf<Product>()
        val db = dbHelper.readableDatabase

        var sql = """
            SELECT p.*, c.name as category_name 
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            WHERE p.user_id = ? AND p.is_active = 1
        """.trimIndent()

        val args = mutableListOf(userId.toString())

        if (categoryId > 0) {
            sql += " AND p.category_id = ?"
            args.add(categoryId.toString())
        }

        if (query.isNotBlank()) {
            sql += " AND (p.name LIKE ? OR p.sku LIKE ?)"
            val q = "%$query%"
            args.add(q)
            args.add(q)
        }

        sql += when (sortBy) {
            "PRICE_LOW_HIGH" -> " ORDER BY p.selling_price_paise ASC"
            "PRICE_HIGH_LOW" -> " ORDER BY p.selling_price_paise DESC"
            "STOCK" -> " ORDER BY p.stock_quantity ASC"
            else -> " ORDER BY p.name ASC"
        }

        val cursor = db.rawQuery(sql, args.toTypedArray())
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

    fun getProductById(id: Long, userId: Long = 1L): Product? {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT p.*, c.name as category_name 
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            WHERE p.id = ? AND p.user_id = ?
        """.trimIndent()
        val cursor = db.rawQuery(sql, arrayOf(id.toString(), userId.toString()))
        cursor.use { c ->
            if (c.moveToFirst()) {
                return Product(
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
            }
        }
        return null
    }

    fun addProduct(product: Product, userId: Long = 1L): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("sku", product.sku)
            put("name", product.name)
            put("category_id", product.categoryId)
            put("purchase_price_paise", product.purchasePricePaise)
            put("selling_price_paise", product.sellingPricePaise)
            put("stock_quantity", product.stockQuantity)
            put("unit", product.unit)
            put("image_path", product.imagePath)
            put("is_active", if (product.isActive) 1 else 0)
            put("created_at", product.createdAt.ifBlank { DateTimeHelper.getCurrentIsoTimestamp() })
        }
        return db.insert("products", null, cv)
    }

    fun updateProduct(product: Product, userId: Long = 1L): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("sku", product.sku)
            put("name", product.name)
            put("category_id", product.categoryId)
            put("purchase_price_paise", product.purchasePricePaise)
            put("selling_price_paise", product.sellingPricePaise)
            put("stock_quantity", product.stockQuantity)
            put("unit", product.unit)
            put("image_path", product.imagePath)
        }
        return db.update("products", cv, "id = ? AND user_id = ?", arrayOf(product.id.toString(), userId.toString())) > 0
    }

    fun deleteProduct(id: Long, userId: Long = 1L): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("is_active", 0)
        }
        return db.update("products", cv, "id = ? AND user_id = ?", arrayOf(id.toString(), userId.toString())) > 0
    }

    fun isSkuAvailable(sku: String, excludeId: Long = 0, userId: Long = 1L): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM products WHERE sku = ? AND user_id = ? AND id != ?",
            arrayOf(sku, userId.toString(), excludeId.toString())
        )
        return cursor.use { !it.moveToFirst() }
    }

    fun getCategories(): List<Category> {
        val list = mutableListOf<Category>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM categories ORDER BY name ASC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Category(
                        id = c.getLongByColumn("id"),
                        name = c.getStringByColumn("name"),
                        description = c.getStringByColumn("description")
                    )
                )
            }
        }
        return list
    }

    fun addCategory(name: String, description: String = ""): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", name)
            put("description", description)
        }
        return db.insert("categories", null, cv)
    }

    fun adjustStock(db: SQLiteDatabase, productId: Long, delta: Int) {
        db.execSQL(
            "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?",
            arrayOf<Any>(delta, productId)
        )
    }
}

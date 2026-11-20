package com.marwadiuniversity.radhe.repositories

import android.content.ContentValues
import android.content.Context
import com.marwadiuniversity.radhe.database.DatabaseHelper
import com.marwadiuniversity.radhe.models.Shop
import com.marwadiuniversity.radhe.models.User
import com.marwadiuniversity.radhe.utils.DateTimeHelper
import com.marwadiuniversity.radhe.utils.PasswordHasher
import com.marwadiuniversity.radhe.utils.getLongByColumn
import com.marwadiuniversity.radhe.utils.getStringByColumn

class AuthRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun login(usernameOrMobile: String, passwordRaw: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE username = ? OR id IN (SELECT user_id FROM staff WHERE phone = ?)",
            arrayOf(usernameOrMobile, usernameOrMobile)
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val salt = c.getStringByColumn("salt")
                val storedHash = c.getStringByColumn("password_hash")
                val inputHash = PasswordHasher.hashPassword(passwordRaw, salt)

                if (storedHash == inputHash) {
                    return User(
                        id = c.getLongByColumn("id"),
                        username = c.getStringByColumn("username"),
                        passwordHash = storedHash,
                        salt = salt,
                        fullName = c.getStringByColumn("full_name"),
                        role = c.getStringByColumn("role"),
                        createdAt = c.getStringByColumn("created_at")
                    )
                }
            }
        }
        return null
    }

    fun registerUser(fullName: String, username: String, passwordRaw: String, role: String = "OWNER"): Long {
        val db = dbHelper.writableDatabase
        val salt = "SmartUdharSalt_" + System.currentTimeMillis()
        val hash = PasswordHasher.hashPassword(passwordRaw, salt)
        val timestamp = DateTimeHelper.getCurrentIsoTimestamp()

        val values = ContentValues().apply {
            put("username", username)
            put("password_hash", hash)
            put("salt", salt)
            put("full_name", fullName)
            put("role", role)
            put("created_at", timestamp)
        }

        return db.insert("users", null, values)
    }

    fun getShopProfile(userId: Long): Shop {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM shops WHERE user_id = ? LIMIT 1", arrayOf(userId.toString()))
        cursor.use { c ->
            if (c.moveToFirst()) {
                return Shop(
                    id = c.getLongByColumn("id"),
                    shopName = c.getStringByColumn("shop_name"),
                    ownerName = c.getStringByColumn("owner_name"),
                    phone = c.getStringByColumn("phone"),
                    address = c.getStringByColumn("address"),
                    createdAt = c.getStringByColumn("created_at")
                )
            }
        }
        return Shop(0L, "", "", "", "")
    }

    fun updateShopProfile(userId: Long, shopName: String, ownerName: String, phone: String, address: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("shop_name", shopName)
            put("owner_name", ownerName)
            put("phone", phone)
            put("address", address)
        }
        val rows = db.update("shops", values, "user_id = ?", arrayOf(userId.toString()))
        if (rows == 0) {
            values.put("created_at", DateTimeHelper.getCurrentIsoTimestamp())
            db.insert("shops", null, values)
        }
        return true
    }
}

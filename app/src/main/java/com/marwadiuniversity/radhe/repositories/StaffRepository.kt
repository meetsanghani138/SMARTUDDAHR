package com.marwadiuniversity.radhe.repositories

import android.content.ContentValues
import android.content.Context
import com.marwadiuniversity.radhe.database.DatabaseHelper
import com.marwadiuniversity.radhe.models.Staff
import com.marwadiuniversity.radhe.utils.DateTimeHelper
import com.marwadiuniversity.radhe.utils.getLongByColumn
import com.marwadiuniversity.radhe.utils.getStringByColumn

class StaffRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun getStaffMembers(): List<Staff> {
        val list = mutableListOf<Staff>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM staff ORDER BY name ASC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Staff(
                        id = c.getLongByColumn("id"),
                        userId = c.getLongByColumn("user_id"),
                        shopId = c.getLongByColumn("shop_id"),
                        name = c.getStringByColumn("name"),
                        phone = c.getStringByColumn("phone"),
                        role = c.getStringByColumn("role"),
                        permissions = c.getStringByColumn("permissions"),
                        createdAt = c.getStringByColumn("created_at")
                    )
                )
            }
        }
        return list
    }

    fun addStaff(name: String, phone: String, role: String, permissions: String = ""): Long {
        val db = dbHelper.writableDatabase
        val timestamp = DateTimeHelper.getCurrentIsoTimestamp()
        val cv = ContentValues().apply {
            put("user_id", 0)
            put("shop_id", 1)
            put("name", name)
            put("phone", phone)
            put("role", role)
            put("permissions", permissions)
            put("created_at", timestamp)
        }
        return db.insert("staff", null, cv)
    }
}

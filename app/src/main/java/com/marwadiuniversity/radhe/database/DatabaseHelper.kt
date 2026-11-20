package com.marwadiuniversity.radhe.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.marwadiuniversity.radhe.utils.DateTimeHelper
import com.marwadiuniversity.radhe.utils.PasswordHasher

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "smart_udhar.db"
        const val DATABASE_VERSION = 2

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'OWNER',
                created_at TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE shops (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL DEFAULT 1,
                shop_name TEXT NOT NULL,
                owner_name TEXT NOT NULL,
                phone TEXT NOT NULL,
                address TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL DEFAULT 1,
                name TEXT NOT NULL,
                mobile TEXT NOT NULL,
                address TEXT,
                opening_balance_paise INTEGER DEFAULT 0,
                current_outstanding_paise INTEGER DEFAULT 0,
                notes TEXT,
                is_archived INTEGER DEFAULT 0,
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                description TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL DEFAULT 1,
                sku TEXT NOT NULL,
                name TEXT NOT NULL,
                category_id INTEGER NOT NULL,
                purchase_price_paise INTEGER NOT NULL,
                selling_price_paise INTEGER NOT NULL,
                stock_quantity INTEGER NOT NULL DEFAULT 0,
                unit TEXT DEFAULT 'Pcs',
                image_path TEXT,
                is_active INTEGER DEFAULT 1,
                created_at TEXT NOT NULL,
                FOREIGN KEY (category_id) REFERENCES categories(id),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL DEFAULT 1,
                transaction_number TEXT UNIQUE NOT NULL,
                customer_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                total_amount_paise INTEGER NOT NULL,
                previous_balance_paise INTEGER NOT NULL,
                new_balance_paise INTEGER NOT NULL,
                created_by TEXT NOT NULL,
                created_at TEXT NOT NULL,
                notes TEXT,
                is_reversed INTEGER DEFAULT 0,
                FOREIGN KEY (customer_id) REFERENCES customers(id),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE transaction_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                transaction_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                unit_price_paise INTEGER NOT NULL,
                total_price_paise INTEGER NOT NULL,
                FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL DEFAULT 1,
                transaction_id INTEGER NOT NULL,
                customer_id INTEGER NOT NULL,
                amount_paise INTEGER NOT NULL,
                payment_method TEXT NOT NULL,
                reference_number TEXT,
                notes TEXT,
                created_at TEXT NOT NULL,
                FOREIGN KEY (transaction_id) REFERENCES transactions(id),
                FOREIGN KEY (customer_id) REFERENCES customers(id),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE staff (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                shop_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                role TEXT NOT NULL,
                permissions TEXT,
                created_at TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE reminders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL DEFAULT 1,
                customer_id INTEGER NOT NULL,
                customer_name TEXT NOT NULL,
                customer_mobile TEXT NOT NULL,
                message TEXT NOT NULL,
                outstanding_amount_paise INTEGER NOT NULL,
                sent_at TEXT NOT NULL,
                language TEXT DEFAULT 'en',
                channel TEXT DEFAULT 'WHATSAPP'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE settings (
                key TEXT PRIMARY KEY,
                value TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE sync_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                entity_type TEXT NOT NULL,
                entity_id INTEGER NOT NULL,
                action TEXT NOT NULL,
                data_json TEXT NOT NULL,
                status TEXT DEFAULT 'PENDING',
                created_at TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action TEXT NOT NULL,
                details TEXT NOT NULL,
                timestamp TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Indexes for speed and fast search
        db.execSQL("CREATE INDEX idx_customers_user ON customers(user_id);")
        db.execSQL("CREATE INDEX idx_products_user ON products(user_id);")
        db.execSQL("CREATE INDEX idx_transactions_user ON transactions(user_id);")
        db.execSQL("CREATE INDEX idx_payments_user ON payments(user_id);")

        seedInitialData(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val timestamp = DateTimeHelper.getCurrentIsoTimestamp()

        // Seed default owner user (Username: admin, Password: password123, ID: 1)
        val salt = "SmartUdharSalt"
        val userValues = ContentValues().apply {
            put("username", "admin")
            put("password_hash", PasswordHasher.hashPassword("password123", salt))
            put("salt", salt)
            put("full_name", "Rajesh Patel")
            put("role", "OWNER")
            put("created_at", timestamp)
        }
        val userId = db.insert("users", null, userValues)

        // Seed default shop for user_id = 1
        val shopValues = ContentValues().apply {
            put("user_id", userId)
            put("shop_name", "Smart Udhar Pan & Kirana")
            put("owner_name", "Rajesh Patel")
            put("phone", "9876543210")
            put("address", "Station Road, Anand, Gujarat")
            put("created_at", timestamp)
        }
        val shopId = db.insert("shops", null, shopValues)

        // Seed default categories (Global)
        val categories = listOf(
            "Mava & Brands",
            "Pan",
            "Snacks",
            "Cold Drinks",
            "Other"
        )
        val categoryIdMap = mutableMapOf<String, Long>()
        for (catName in categories) {
            val cv = ContentValues().apply {
                put("name", catName)
                put("description", "$catName items")
            }
            val catId = db.insert("categories", null, cv)
            categoryIdMap[catName] = catId
        }

        // Seed sample products for user_id = 1 (Admin) ONLY
        val sampleProducts = listOf(
            arrayOf("SKU001", "Vimal", "Mava & Brands", "800", "1000", "100", "Pcs"),
            arrayOf("SKU002", "Rajnigandha", "Mava & Brands", "4200", "5000", "50", "Pcs"),
            arrayOf("SKU003", "RMD", "Mava & Brands", "1800", "2000", "60", "Pcs"),
            arrayOf("SKU004", "Sadu Pan", "Pan", "1200", "2000", "80", "Pcs"),
            arrayOf("SKU005", "Mithu Pan", "Pan", "1500", "2500", "75", "Pcs"),
            arrayOf("SKU006", "Balaji Wafers", "Snacks", "1600", "2000", "40", "Packet"),
            arrayOf("SKU007", "Cold Drink", "Cold Drinks", "3000", "4000", "30", "Bottle"),
            arrayOf("SKU008", "Water Bottle", "Cold Drinks", "1200", "2000", "50", "Bottle"),
            arrayOf("SKU009", "Biscuits", "Snacks", "2200", "3000", "45", "Packet"),
            arrayOf("SKU010", "Namkeen", "Snacks", "3800", "5000", "25", "Packet")
        )

        for (p in sampleProducts) {
            val catId = categoryIdMap[p[2]] ?: 1L
            val pv = ContentValues().apply {
                put("user_id", userId)
                put("sku", p[0])
                put("name", p[1])
                put("category_id", catId)
                put("purchase_price_paise", p[3].toLong())
                put("selling_price_paise", p[4].toLong())
                put("stock_quantity", p[5].toInt())
                put("unit", p[6])
                put("is_active", 1)
                put("created_at", timestamp)
            }
            db.insert("products", null, pv)
        }

        // Seed sample customers for user_id = 1 (Admin) ONLY
        val sampleCustomers = listOf(
            arrayOf("Ramesh Sharma", "9898011223", "Sector 1, Anand", "0", "15000", "Regular customer"),
            arrayOf("Suresh Kumar", "9723044556", "Main Bazaar, Anand", "0", "35000", "Daily pan buyer"),
            arrayOf("Amit Shah", "9825066778", "V.V. Nagar", "0", "0", "Prompt payment")
        )

        for (c in sampleCustomers) {
            val cv = ContentValues().apply {
                put("user_id", userId)
                put("name", c[0])
                put("mobile", c[1])
                put("address", c[2])
                put("opening_balance_paise", c[3].toLong())
                put("current_outstanding_paise", c[4].toLong())
                put("notes", c[5])
                put("is_archived", 0)
                put("created_at", timestamp)
            }
            db.insert("customers", null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try { db.execSQL("ALTER TABLE shops ADD COLUMN user_id INTEGER DEFAULT 1;") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE customers ADD COLUMN user_id INTEGER DEFAULT 1;") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE products ADD COLUMN user_id INTEGER DEFAULT 1;") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE transactions ADD COLUMN user_id INTEGER DEFAULT 1;") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE payments ADD COLUMN user_id INTEGER DEFAULT 1;") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE reminders ADD COLUMN user_id INTEGER DEFAULT 1;") } catch (_: Exception) {}
        }
    }
}

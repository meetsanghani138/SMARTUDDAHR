package com.marwadiuniversity.radhe.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_udhar_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_SHOP_ID = "shop_id"
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_SHOP_PHONE = "shop_phone"
        private const val KEY_SHOP_ADDRESS = "shop_address"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_APP_LOCK_PIN = "app_lock_pin"
    }

    fun saveUserSession(
        userId: Long,
        userName: String,
        role: String,
        shopId: Long,
        shopName: String,
        shopPhone: String,
        shopAddress: String
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_ROLE, role)
            putLong(KEY_SHOP_ID, shopId)
            putString(KEY_SHOP_NAME, shopName)
            putString(KEY_SHOP_PHONE, shopPhone)
            putString(KEY_SHOP_ADDRESS, shopAddress)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Shopkeeper") ?: "Shopkeeper"
    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "OWNER") ?: "OWNER"
    
    fun getShopId(): Long = prefs.getLong(KEY_SHOP_ID, 1L)
    fun getShopName(): String = prefs.getString(KEY_SHOP_NAME, "Smart Udhar Shop") ?: "Smart Udhar Shop"
    fun getShopPhone(): String = prefs.getString(KEY_SHOP_PHONE, "9876543210") ?: "9876543210"
    fun getShopAddress(): String = prefs.getString(KEY_SHOP_ADDRESS, "Main Market, Gujarat") ?: "Main Market, Gujarat"

    fun updateShopDetails(name: String, phone: String, address: String) {
        prefs.edit().apply {
            putString(KEY_SHOP_NAME, name)
            putString(KEY_SHOP_PHONE, phone)
            putString(KEY_SHOP_ADDRESS, address)
            apply()
        }
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
    fun setAppLock(enabled: Boolean, pin: String = "") {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).putString(KEY_APP_LOCK_PIN, pin).apply()
    }
    fun getAppLockPin(): String = prefs.getString(KEY_APP_LOCK_PIN, "") ?: ""

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isOwner(): Boolean = getUserRole() == "OWNER"
    fun isManagerOrAbove(): Boolean = getUserRole() == "OWNER" || getUserRole() == "MANAGER"
}

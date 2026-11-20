package com.marwadiuniversity.radhe.utils

import java.security.MessageDigest

object PasswordHasher {
    fun hashPassword(password: String, salt: String = "SmartUdharSalt"): String {
        val input = password + salt
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hash: String, salt: String = "SmartUdharSalt"): Boolean {
        return hashPassword(password, salt) == hash
    }
}

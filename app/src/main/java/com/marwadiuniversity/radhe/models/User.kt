package com.marwadiuniversity.radhe.models

data class User(
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val fullName: String,
    val role: String = "OWNER",
    val createdAt: String = ""
)

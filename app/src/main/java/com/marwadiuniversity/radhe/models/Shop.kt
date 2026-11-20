package com.marwadiuniversity.radhe.models

data class Shop(
    val id: Long = 0,
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val createdAt: String = ""
)

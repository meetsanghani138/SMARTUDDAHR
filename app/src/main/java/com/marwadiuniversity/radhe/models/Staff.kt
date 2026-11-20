package com.marwadiuniversity.radhe.models

import java.io.Serializable

data class Staff(
    val id: Long = 0,
    val userId: Long,
    val shopId: Long,
    val name: String,
    val phone: String,
    val role: String, // OWNER, MANAGER, STAFF
    val permissions: String = "",
    val createdAt: String = ""
) : Serializable

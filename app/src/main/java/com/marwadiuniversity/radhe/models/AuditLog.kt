package com.marwadiuniversity.radhe.models

data class AuditLog(
    val id: Long = 0,
    val userId: Long,
    val userName: String = "",
    val action: String,
    val details: String,
    val timestamp: String = ""
)

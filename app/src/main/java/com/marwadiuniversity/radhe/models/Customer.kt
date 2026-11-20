package com.marwadiuniversity.radhe.models

import java.io.Serializable

data class Customer(
    val id: Long = 0,
    val name: String,
    val mobile: String,
    val address: String = "",
    val openingBalancePaise: Long = 0L,
    val currentOutstandingPaise: Long = 0L,
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: String = ""
) : Serializable

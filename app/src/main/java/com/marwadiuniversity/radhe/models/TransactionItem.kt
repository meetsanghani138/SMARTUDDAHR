package com.marwadiuniversity.radhe.models

import java.io.Serializable

data class TransactionItem(
    val id: Long = 0,
    val transactionId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPricePaise: Long,
    val totalPricePaise: Long
) : Serializable

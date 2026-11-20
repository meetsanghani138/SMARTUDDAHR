package com.marwadiuniversity.radhe.models

import java.io.Serializable

data class Payment(
    val id: Long = 0,
    val transactionId: Long,
    val customerId: Long,
    val customerName: String = "",
    val amountPaise: Long,
    val paymentMethod: String, // CASH, UPI, BANK_TRANSFER
    val referenceNumber: String = "",
    val notes: String = "",
    val createdAt: String = ""
) : Serializable

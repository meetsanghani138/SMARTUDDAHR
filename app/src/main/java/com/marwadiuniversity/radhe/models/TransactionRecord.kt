package com.marwadiuniversity.radhe.models

import java.io.Serializable

data class TransactionRecord(
    val id: Long = 0,
    val transactionNumber: String,
    val customerId: Long,
    val customerName: String = "",
    val customerMobile: String = "",
    val type: String, // CREDIT_PURCHASE, PAYMENT_RECEIVED, REVERSAL, ADJUSTMENT
    val totalAmountPaise: Long,
    val previousBalancePaise: Long,
    val newBalancePaise: Long,
    val createdBy: String = "Owner",
    val createdAt: String = "",
    val notes: String = "",
    val isReversed: Boolean = false,
    val items: List<TransactionItem> = emptyList()
) : Serializable

package com.marwadiuniversity.radhe.models

import java.io.Serializable

data class Reminder(
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerMobile: String,
    val message: String,
    val outstandingAmountPaise: Long,
    val sentAt: String,
    val language: String = "en",
    val channel: String = "WHATSAPP"
) : Serializable

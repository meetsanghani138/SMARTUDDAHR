package com.marwadiuniversity.radhe.models

import java.io.Serializable

data class Product(
    val id: Long = 0,
    val sku: String,
    val name: String,
    val categoryId: Long,
    val categoryName: String = "",
    val purchasePricePaise: Long,
    val sellingPricePaise: Long,
    val stockQuantity: Int,
    val unit: String = "Pcs",
    val imagePath: String = "",
    val isActive: Boolean = true,
    val createdAt: String = ""
) : Serializable

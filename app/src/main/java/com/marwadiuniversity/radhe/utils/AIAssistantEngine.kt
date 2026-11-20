package com.marwadiuniversity.radhe.utils

import android.content.Context
import com.marwadiuniversity.radhe.repositories.CustomerRepository
import com.marwadiuniversity.radhe.repositories.ProductRepository
import com.marwadiuniversity.radhe.repositories.ReportRepository

data class AIAnswer(
    val title: String,
    val text: String,
    val detailsList: List<String> = emptyList()
)

class AIAssistantEngine(context: Context) {
    private val reportRepo = ReportRepository(context)
    private val customerRepo = CustomerRepository(context)
    private val productRepo = ProductRepository(context)
    private val sessionManager = SessionManager(context)

    fun answerQuery(userQuery: String): AIAnswer {
        val query = userQuery.lowercase().trim()
        val userId = sessionManager.getUserId()

        return when {
            query.contains("udhar") || query.contains("pending") || query.contains("outstanding") || query.contains("બાકી") || query.contains("बकाया") -> {
                val summary = reportRepo.getDashboardSummary(userId)
                val topCust = reportRepo.getTopOutstandingCustomers(3, userId)
                val custDetails = topCust.map { "${it.name}: ${CurrencyFormatter.formatPaiseToRupees(it.currentOutstandingPaise)}" }
                AIAnswer(
                    title = "Pending Udhar Summary",
                    text = "Total outstanding Udhar is ${CurrencyFormatter.formatPaiseToRupees(summary.totalUdharPaise)} across ${summary.totalCustomers} customers.",
                    detailsList = custDetails
                )
            }

            query.contains("today") || query.contains("sales") || query.contains("આજ") || query.contains("आज") -> {
                val summary = reportRepo.getDashboardSummary(userId)
                AIAnswer(
                    title = "Today's Business Summary",
                    text = "Today's total sales are ${CurrencyFormatter.formatPaiseToRupees(summary.todaysSalesPaise)} and payments received today are ${CurrencyFormatter.formatPaiseToRupees(summary.paymentsReceivedPaise)}.",
                    detailsList = listOf(
                        "Sales (Udhar): ${CurrencyFormatter.formatPaiseToRupees(summary.todaysSalesPaise)}",
                        "Collected: ${CurrencyFormatter.formatPaiseToRupees(summary.paymentsReceivedPaise)}"
                    )
                )
            }

            query.contains("stock") || query.contains("low") || query.contains("જથ્થો") || query.contains("સ્ટોક") -> {
                val lowStock = reportRepo.getLowStockProducts(10, userId)
                if (lowStock.isEmpty()) {
                    AIAnswer(
                        title = "Inventory Alert",
                        text = "All products have sufficient stock! No low stock alerts.",
                        detailsList = emptyList()
                    )
                } else {
                    val details = lowStock.map { "${it.name}: ${it.stockQuantity} ${it.unit} left" }
                    AIAnswer(
                        title = "Low Stock Warning",
                        text = "There are ${lowStock.size} products running low on stock.",
                        detailsList = details
                    )
                }
            }

            query.contains("product") || query.contains("item") || query.contains("સામગ્રી") || query.contains("सामग्री") -> {
                val products = productRepo.getAllProducts(userId = userId)
                val details = products.take(5).map { "${it.name} - Selling: ${CurrencyFormatter.formatPaiseToRupees(it.sellingPricePaise)} (Stock: ${it.stockQuantity})" }
                AIAnswer(
                    title = "Product Catalogue",
                    text = "You have ${products.size} active products in your inventory.",
                    detailsList = details
                )
            }

            query.contains("customer") || query.contains("client") || query.contains("ગ્રાહક") || query.contains("ग्राहक") -> {
                val customers = customerRepo.getAllCustomers(userId = userId)
                val details = customers.take(5).map { "${it.name} - Outstanding: ${CurrencyFormatter.formatPaiseToRupees(it.currentOutstandingPaise)}" }
                AIAnswer(
                    title = "Customer Overview",
                    text = "You have ${customers.size} registered customers.",
                    detailsList = details
                )
            }

            else -> {
                val summary = reportRepo.getDashboardSummary(userId)
                AIAnswer(
                    title = "Smart Udhar AI Assistant",
                    text = "Here is a quick snapshot of your business:\n" +
                            "• Today's Sales: ${CurrencyFormatter.formatPaiseToRupees(summary.todaysSalesPaise)}\n" +
                            "• Total Pending Udhar: ${CurrencyFormatter.formatPaiseToRupees(summary.totalUdharPaise)}\n" +
                            "• Low Stock Items: ${summary.lowStockCount}",
                    detailsList = listOf(
                        "Try asking: 'How much udhar is pending?'",
                        "Try asking: 'Which products are low in stock?'",
                        "Try asking: 'What are today's sales?'"
                    )
                )
            }
        }
    }
}

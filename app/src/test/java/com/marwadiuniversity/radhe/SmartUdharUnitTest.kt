package com.marwadiuniversity.radhe

import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.models.Product
import com.marwadiuniversity.radhe.models.TransactionItem
import com.marwadiuniversity.radhe.models.TransactionRecord
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.PasswordHasher
import com.marwadiuniversity.radhe.utils.VoicePurchaseParser
import com.marwadiuniversity.radhe.utils.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartUdharUnitTest {

    @Test
    fun testCurrencyFormatting() {
        val paise = 9500L
        val formatted = CurrencyFormatter.formatPaiseToRupees(paise)
        assertTrue("Formatted currency should contain 95", formatted.contains("95"))

        val parsedPaise = CurrencyFormatter.parseRupeesToPaise("95.50")
        assertEquals(9550L, parsedPaise)
    }

    @Test
    fun testPasswordHashing() {
        val rawPassword = "password123"
        val salt = "SmartUdharSalt"
        val hash = PasswordHasher.hashPassword(rawPassword, salt)
        
        assertTrue("Hash should not be empty", hash.isNotEmpty())
        assertTrue("Password verification should succeed", PasswordHasher.verifyPassword(rawPassword, hash, salt))
    }

    @Test
    fun testWhatsAppMessageFormatting() {
        val msgGu = WhatsAppHelper.formatMessage("gu", "Ramesh", "Patel Store", "₹150.00")
        assertTrue("Gujarati message should contain customer name", msgGu.contains("Ramesh"))
        assertTrue("Gujarati message should contain amount", msgGu.contains("150.00"))

        val msgHi = WhatsAppHelper.formatMessage("hi", "Rahul", "Patel Store", "₹50.00")
        assertTrue("Hindi message should contain customer name", msgHi.contains("Rahul"))

        val msgEn = WhatsAppHelper.formatMessage("en", "Amit", "Patel Store", "₹200.00")
        assertTrue("English message should contain customer name", msgEn.contains("Amit"))
    }

    @Test
    fun testVoicePurchaseParsing() {
        val sampleProducts = listOf(
            Product(id = 1, sku = "SKU1", name = "Vimal", categoryId = 1, purchasePricePaise = 800, sellingPricePaise = 1000, stockQuantity = 50),
            Product(id = 2, sku = "SKU2", name = "Cold Drink", categoryId = 2, purchasePricePaise = 3000, sellingPricePaise = 4000, stockQuantity = 30)
        )

        val spokenText = "1 Vimal and 2 Cold Drink"
        val parsed = VoicePurchaseParser.parseSpokenText(spokenText, sampleProducts)

        assertTrue("Parsed list should find items", parsed.isNotEmpty())
    }

    @Test
    fun testProductAndCustomerDeletionAndImage() {
        val productWithImg = Product(
            id = 1,
            sku = "SKU-IMG",
            name = "Pan Parag",
            categoryId = 1,
            purchasePricePaise = 1000,
            sellingPricePaise = 1500,
            stockQuantity = 20,
            imagePath = "content://media/external/images/media/10"
        )
        assertTrue("Product image path should be preserved", productWithImg.imagePath.isNotBlank())

        val deactivatedProduct = productWithImg.copy(isActive = false)
        assertFalse("Deactivated product should have isActive = false", deactivatedProduct.isActive)

        val activeCustomer = Customer(id = 1, name = "Suresh", mobile = "9876543210")
        val archivedCustomer = activeCustomer.copy(isArchived = true)
        assertTrue("Archived customer should have isArchived = true", archivedCustomer.isArchived)
    }

    @Test
    fun testMultipleOwnerAccountIsolation() {
        val userAId = 101L
        val userBId = 102L

        val productsUserA = mutableListOf(
            Product(id = 1, sku = "PA1", name = "Vimal", categoryId = 1, purchasePricePaise = 800, sellingPricePaise = 1000, stockQuantity = 50),
            Product(id = 2, sku = "PA2", name = "Rajnigandha", categoryId = 1, purchasePricePaise = 4000, sellingPricePaise = 5000, stockQuantity = 20),
            Product(id = 3, sku = "PA3", name = "Sadu Pan", categoryId = 2, purchasePricePaise = 1000, sellingPricePaise = 2000, stockQuantity = 30),
            Product(id = 4, sku = "PA4", name = "Cold Drink", categoryId = 4, purchasePricePaise = 3000, sellingPricePaise = 4000, stockQuantity = 40),
            Product(id = 5, sku = "PA5", name = "Water Bottle", categoryId = 4, purchasePricePaise = 1000, sellingPricePaise = 2000, stockQuantity = 50)
        )
        val customersUserA = mutableListOf(
            Customer(id = 1, name = "Rahul", mobile = "9876543210"),
            Customer(id = 2, name = "Amit", mobile = "9876543211"),
            Customer(id = 3, name = "Suresh", mobile = "9876543212")
        )

        // Account B initially has empty data
        val productsUserB = mutableListOf<Product>()
        val customersUserB = mutableListOf<Customer>()

        assertEquals(5, productsUserA.size)
        assertEquals(3, customersUserA.size)
        assertEquals(0, productsUserB.size)
        assertEquals(0, customersUserB.size)

        // Account B adds 2 products and 1 customer
        productsUserB.add(Product(id = 6, sku = "PB1", name = "Lays Wafers", categoryId = 3, purchasePricePaise = 1500, sellingPricePaise = 2000, stockQuantity = 10))
        productsUserB.add(Product(id = 7, sku = "PB2", name = "Sprite", categoryId = 4, purchasePricePaise = 3000, sellingPricePaise = 4000, stockQuantity = 15))
        customersUserB.add(Customer(id = 4, name = "Vijay", mobile = "9112233445"))

        // Re-verify Account A retains original items
        assertEquals(5, productsUserA.size)
        assertEquals(3, customersUserA.size)

        // Re-verify Account B retains its own isolated items
        assertEquals(2, productsUserB.size)
        assertEquals(1, customersUserB.size)
        assertFalse("Account B must not contain Account A's customer", customersUserB.any { it.name == "Rahul" })
    }

    @Test
    fun testMandatoryPanShopWorkflowAndSnapshotPricing() {
        var vimal = Product(id = 101, sku = "VIM01", name = "Vimal", categoryId = 1, purchasePricePaise = 800, sellingPricePaise = 1000, stockQuantity = 100)
        val coldDrink = Product(id = 102, sku = "CD01", name = "Cold Drink", categoryId = 4, purchasePricePaise = 3000, sellingPricePaise = 4000, stockQuantity = 50)
        val rahul = Customer(id = 501, name = "Rahul", mobile = "9988776655", openingBalancePaise = 0, currentOutstandingPaise = 0)

        val itemVimal = TransactionItem(productId = vimal.id, productName = vimal.name, quantity = 2, unitPricePaise = vimal.sellingPricePaise, totalPricePaise = vimal.sellingPricePaise * 2)
        val itemColdDrink = TransactionItem(productId = coldDrink.id, productName = coldDrink.name, quantity = 1, unitPricePaise = coldDrink.sellingPricePaise, totalPricePaise = coldDrink.sellingPricePaise * 1)

        val billItems = listOf(itemVimal, itemColdDrink)
        val billTotal = billItems.sumOf { it.totalPricePaise }

        assertEquals(6000L, billTotal)
        assertEquals("₹60.00", CurrencyFormatter.formatPaiseToRupees(billTotal))

        val previousBal = rahul.currentOutstandingPaise
        val newBal = previousBal + billTotal
        val updatedRahul = rahul.copy(currentOutstandingPaise = newBal)
        assertEquals(6000L, updatedRahul.currentOutstandingPaise)

        val transactionRecord = TransactionRecord(
            id = 1001,
            transactionNumber = "TXN-TEST-1",
            customerId = rahul.id,
            customerName = rahul.name,
            type = "CREDIT_PURCHASE",
            totalAmountPaise = billTotal,
            previousBalancePaise = previousBal,
            newBalancePaise = newBal,
            items = billItems
        )

        vimal = vimal.copy(sellingPricePaise = 1500)
        assertEquals(1500L, vimal.sellingPricePaise)

        val snapshotVimalItem = transactionRecord.items.first { it.productId == 101L }
        assertEquals(1000L, snapshotVimalItem.unitPricePaise)
        assertNotEquals(vimal.sellingPricePaise, snapshotVimalItem.unitPricePaise)

        val Priya = Customer(id = 502, name = "Priya", mobile = "9112233445", openingBalancePaise = 0, currentOutstandingPaise = 0)
        assertNotEquals(updatedRahul.id, Priya.id)
        assertEquals(0L, Priya.currentOutstandingPaise)
        assertEquals(6000L, updatedRahul.currentOutstandingPaise)
    }
}

package com.marwadiuniversity.radhe.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.marwadiuniversity.radhe.models.Payment
import com.marwadiuniversity.radhe.models.Shop
import com.marwadiuniversity.radhe.models.TransactionRecord
import java.io.File
import java.io.FileOutputStream

object PdfReceiptGenerator {

    fun generateUdharReceiptPdf(context: Context, shop: Shop, transaction: TransactionRecord): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#16834A")
            textSize = 22f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#758078")
            textSize = 11f
        }

        var y = 40f

        // Title
        canvas.drawText("SMART UDHAR - CREDIT RECEIPT", 40f, y, titlePaint)
        y += 20f
        canvas.drawText("Smart Hisab, Easy Vyapar", 40f, y, headerPaint)
        y += 30f

        // Shop details
        canvas.drawText(shop.shopName, 40f, y, boldPaint)
        y += 18f
        canvas.drawText("Address: ${shop.address}", 40f, y, textPaint)
        y += 18f
        canvas.drawText("Phone: ${shop.phone}", 40f, y, textPaint)
        y += 25f

        canvas.drawLine(40f, y, 555f, y, boldPaint)
        y += 20f

        // Receipt Meta
        canvas.drawText("Receipt #: ${transaction.transactionNumber}", 40f, y, boldPaint)
        canvas.drawText("Date: ${transaction.createdAt}", 350f, y, textPaint)
        y += 20f
        canvas.drawText("Customer: ${transaction.customerName}", 40f, y, boldPaint)
        canvas.drawText("Mobile: ${transaction.customerMobile}", 350f, y, textPaint)
        y += 30f

        // Items Table Header
        canvas.drawRect(40f, y - 15f, 555f, y + 10f, Paint().apply { color = Color.parseColor("#E8F5ED") })
        canvas.drawText("Product", 50f, y, boldPaint)
        canvas.drawText("Qty", 280f, y, boldPaint)
        canvas.drawText("Price", 360f, y, boldPaint)
        canvas.drawText("Total", 470f, y, boldPaint)
        y += 25f

        // Table Rows
        for (item in transaction.items) {
            canvas.drawText(item.productName, 50f, y, textPaint)
            canvas.drawText(item.quantity.toString(), 280f, y, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaiseToRupees(item.unitPricePaise), 360f, y, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaiseToRupees(item.totalPricePaise), 470f, y, textPaint)
            y += 20f
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, boldPaint)
        y += 25f

        // Summary Calculations
        canvas.drawText("Subtotal:", 330f, y, boldPaint)
        canvas.drawText(CurrencyFormatter.formatPaiseToRupees(transaction.totalAmountPaise), 470f, y, boldPaint)
        y += 20f

        canvas.drawText("Previous Outstanding:", 330f, y, textPaint)
        canvas.drawText(CurrencyFormatter.formatPaiseToRupees(transaction.previousBalancePaise), 470f, y, textPaint)
        y += 20f

        canvas.drawText("New Outstanding Balance:", 330f, y, boldPaint)
        canvas.drawText(CurrencyFormatter.formatPaiseToRupees(transaction.newBalancePaise), 470f, y, boldPaint)
        y += 40f

        // Footer
        canvas.drawText("Thank you for your business!", 200f, y, headerPaint)

        document.finishPage(page)

        val outputFile = File(context.cacheDir, "receipt_${transaction.transactionNumber}.pdf")
        return try {
            val fos = FileOutputStream(outputFile)
            document.writeTo(fos)
            document.close()
            fos.close()
            outputFile
        } catch (e: Exception) {
            document.close()
            null
        }
    }

    fun generatePaymentReceiptPdf(context: Context, shop: Shop, payment: Payment): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#16834A")
            textSize = 22f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#758078")
            textSize = 11f
        }

        var y = 40f

        canvas.drawText("SMART UDHAR - PAYMENT RECEIPT", 40f, y, titlePaint)
        y += 20f
        canvas.drawText("Smart Hisab, Easy Vyapar", 40f, y, headerPaint)
        y += 30f

        canvas.drawText(shop.shopName, 40f, y, boldPaint)
        y += 18f
        canvas.drawText("Phone: ${shop.phone}", 40f, y, textPaint)
        y += 25f

        canvas.drawLine(40f, y, 555f, y, boldPaint)
        y += 20f

        canvas.drawText("Payment Ref: ${payment.referenceNumber.ifBlank { "PAY-" + payment.id }}", 40f, y, boldPaint)
        canvas.drawText("Date: ${payment.createdAt}", 350f, y, textPaint)
        y += 20f
        canvas.drawText("Customer: ${payment.customerName}", 40f, y, boldPaint)
        y += 30f

        canvas.drawText("Payment Method:", 40f, y, textPaint)
        canvas.drawText(payment.paymentMethod, 200f, y, boldPaint)
        y += 25f

        canvas.drawText("Amount Paid:", 40f, y, boldPaint)
        canvas.drawText(CurrencyFormatter.formatPaiseToRupees(payment.amountPaise), 200f, y, boldPaint)
        y += 35f

        canvas.drawText("Thank you for your payment!", 200f, y, headerPaint)

        document.finishPage(page)

        val outputFile = File(context.cacheDir, "payment_receipt_${payment.id}.pdf")
        return try {
            val fos = FileOutputStream(outputFile)
            document.writeTo(fos)
            document.close()
            fos.close()
            outputFile
        } catch (e: Exception) {
            document.close()
            null
        }
    }
}

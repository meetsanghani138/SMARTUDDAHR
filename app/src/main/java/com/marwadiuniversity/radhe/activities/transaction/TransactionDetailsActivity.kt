package com.marwadiuniversity.radhe.activities.transaction

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.marwadiuniversity.radhe.adapters.BillItemAdapter
import com.marwadiuniversity.radhe.databinding.ActivityTransactionDetailsBinding
import com.marwadiuniversity.radhe.models.TransactionRecord
import com.marwadiuniversity.radhe.repositories.AuthRepository
import com.marwadiuniversity.radhe.repositories.TransactionRepository
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.PdfReceiptGenerator
import com.marwadiuniversity.radhe.utils.SessionManager

class TransactionDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionDetailsBinding
    private lateinit var txnRepo: TransactionRepository
    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: SessionManager

    private var transaction: TransactionRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        txnRepo = TransactionRepository(this)
        authRepo = AuthRepository(this)
        sessionManager = SessionManager(this)

        val userId = sessionManager.getUserId()
        val txnId = intent.getLongExtra("TRANSACTION_ID", 0L)
        transaction = txnRepo.getTransactionById(txnId, userId)

        if (transaction == null) {
            finish()
            return
        }

        displayDetails()

        binding.btnExportPdf.setOnClickListener {
            val shop = authRepo.getShopProfile(userId)
            val pdfFile = PdfReceiptGenerator.generateUdharReceiptPdf(this, shop, transaction!!)
            if (pdfFile != null && pdfFile.exists()) {
                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", pdfFile)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Receipt PDF"))
            } else {
                Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnReverseTxn.setOnClickListener {
            val success = txnRepo.reverseTransaction(transaction!!.id, "User requested reversal", userId)
            if (success) {
                Toast.makeText(this, "Transaction reversed!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Transaction already reversed or invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayDetails() {
        val t = transaction ?: return
        binding.tvTxnNumber.text = t.transactionNumber
        binding.tvTxnDate.text = t.createdAt
        binding.tvCustomerInfo.text = "Customer: ${t.customerName} (${t.customerMobile})"
        binding.tvTotalAmount.text = CurrencyFormatter.formatPaiseToRupees(t.totalAmountPaise)

        binding.rvTxnItems.layoutManager = LinearLayoutManager(this)
        binding.rvTxnItems.adapter = BillItemAdapter(t.items)
    }
}

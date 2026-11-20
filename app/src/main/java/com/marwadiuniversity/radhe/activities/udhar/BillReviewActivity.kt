package com.marwadiuniversity.radhe.activities.udhar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.marwadiuniversity.radhe.MainActivity
import com.marwadiuniversity.radhe.adapters.BillItemAdapter
import com.marwadiuniversity.radhe.databinding.ActivityBillReviewBinding
import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.models.TransactionItem
import com.marwadiuniversity.radhe.repositories.CustomerRepository
import com.marwadiuniversity.radhe.repositories.TransactionRepository
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.SessionManager

class BillReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBillReviewBinding
    private lateinit var customerRepo: CustomerRepository
    private lateinit var txnRepo: TransactionRepository
    private lateinit var sessionManager: SessionManager

    private var customer: Customer? = null
    private var items: List<TransactionItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customerRepo = CustomerRepository(this)
        txnRepo = TransactionRepository(this)
        sessionManager = SessionManager(this)

        val userId = sessionManager.getUserId()
        val customerId = intent.getLongExtra("CUSTOMER_ID", 0L)
        items = (intent.getSerializableExtra("ITEMS") as? ArrayList<TransactionItem>) ?: emptyList()

        customer = customerRepo.getCustomerById(customerId, userId)
        if (customer == null || items.isEmpty()) {
            finish()
            return
        }

        displayBillDetails()

        binding.btnConfirmSave.setOnClickListener {
            binding.btnConfirmSave.isEnabled = false // Prevent double submit

            val notes = binding.etNotes.text.toString().trim()
            val txn = txnRepo.createUdharTransaction(
                customerId = customer!!.id,
                items = items,
                createdBy = sessionManager.getUserName(),
                notes = notes,
                userId = userId
            )

            if (txn != null) {
                Toast.makeText(this, "Udhar recorded successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            } else {
                binding.btnConfirmSave.isEnabled = true
                Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayBillDetails() {
        binding.tvCustomerName.text = customer?.name
        binding.rvBillItems.layoutManager = LinearLayoutManager(this)
        binding.rvBillItems.adapter = BillItemAdapter(items)

        val totalAmountPaise = items.sumOf { it.totalPricePaise }
        val previousBalance = customer?.currentOutstandingPaise ?: 0L
        val newOutstanding = previousBalance + totalAmountPaise

        binding.tvNewCredit.text = CurrencyFormatter.formatPaiseToRupees(totalAmountPaise)
        binding.tvPreviousBalance.text = CurrencyFormatter.formatPaiseToRupees(previousBalance)
        binding.tvNewOutstanding.text = CurrencyFormatter.formatPaiseToRupees(newOutstanding)
    }
}

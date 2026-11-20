package com.marwadiuniversity.radhe.activities.customer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.marwadiuniversity.radhe.activities.payment.ReceivePaymentActivity
import com.marwadiuniversity.radhe.activities.transaction.TransactionDetailsActivity
import com.marwadiuniversity.radhe.activities.udhar.AddUdharActivity
import com.marwadiuniversity.radhe.adapters.TransactionAdapter
import com.marwadiuniversity.radhe.databinding.ActivityCustomerDetailsBinding
import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.repositories.CustomerRepository
import com.marwadiuniversity.radhe.repositories.TransactionRepository
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.SessionManager
import com.marwadiuniversity.radhe.utils.WhatsAppHelper

class CustomerDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerDetailsBinding
    private lateinit var customerRepo: CustomerRepository
    private lateinit var txnRepo: TransactionRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TransactionAdapter

    private var customer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customer = intent.getSerializableExtra("CUSTOMER") as? Customer
        if (customer == null) {
            finish()
            return
        }

        customerRepo = CustomerRepository(this)
        txnRepo = TransactionRepository(this)
        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupListeners()
        loadCustomerDetails()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList()) { txn ->
            val intent = Intent(this, TransactionDetailsActivity::class.java).apply {
                putExtra("TRANSACTION_ID", txn.id)
            }
            startActivity(intent)
        }
        binding.rvCustomerTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvCustomerTransactions.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnDeleteCustomer.setOnClickListener {
            val c = customer ?: return@setOnClickListener
            val userId = sessionManager.getUserId()
            AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setMessage("Are you sure you want to delete ${c.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    customerRepo.archiveCustomer(c.id, userId)
                    Toast.makeText(this, "Customer deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnAddUdhar.setOnClickListener {
            val intent = Intent(this, AddUdharActivity::class.java).apply {
                putExtra("CUSTOMER_ID", customer?.id ?: 0L)
            }
            startActivity(intent)
        }

        binding.btnReceivePayment.setOnClickListener {
            val intent = Intent(this, ReceivePaymentActivity::class.java).apply {
                putExtra("CUSTOMER_ID", customer?.id ?: 0L)
            }
            startActivity(intent)
        }

        binding.btnSendWhatsApp.setOnClickListener {
            val c = customer ?: return@setOnClickListener
            WhatsAppHelper.sendWhatsAppReminder(
                context = this,
                customerId = c.id,
                customerName = c.name,
                customerMobile = c.mobile,
                shopName = sessionManager.getShopName(),
                outstandingPaise = c.currentOutstandingPaise,
                language = sessionManager.getLanguage()
            )
        }
    }

    private fun loadCustomerDetails() {
        val cId = customer?.id ?: return
        val userId = sessionManager.getUserId()
        val freshCustomer = customerRepo.getCustomerById(cId, userId) ?: customer!!
        customer = freshCustomer

        binding.tvCustomerName.text = freshCustomer.name
        binding.tvCustomerMobile.text = freshCustomer.mobile
        binding.tvOutstandingBalance.text = CurrencyFormatter.formatPaiseToRupees(freshCustomer.currentOutstandingPaise)

        val txns = txnRepo.getAllTransactions(customerId = cId, userId = userId)
        adapter.updateList(txns)
    }

    override fun onResume() {
        super.onResume()
        if (customer != null) {
            loadCustomerDetails()
        }
    }
}

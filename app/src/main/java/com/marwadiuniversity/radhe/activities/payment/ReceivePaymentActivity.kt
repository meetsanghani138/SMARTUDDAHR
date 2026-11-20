package com.marwadiuniversity.radhe.activities.payment

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.MainActivity
import com.marwadiuniversity.radhe.databinding.ActivityReceivePaymentBinding
import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.repositories.CustomerRepository
import com.marwadiuniversity.radhe.repositories.PaymentRepository
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.SessionManager

class ReceivePaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceivePaymentBinding
    private lateinit var customerRepo: CustomerRepository
    private lateinit var paymentRepo: PaymentRepository
    private lateinit var sessionManager: SessionManager

    private var customer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceivePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customerRepo = CustomerRepository(this)
        paymentRepo = PaymentRepository(this)
        sessionManager = SessionManager(this)

        val userId = sessionManager.getUserId()
        val customerId = intent.getLongExtra("CUSTOMER_ID", 0L)
        if (customerId > 0) {
            customer = customerRepo.getCustomerById(customerId, userId)
        } else {
            val allCust = customerRepo.getAllCustomers(userId = userId)
            customer = allCust.firstOrNull()
        }

        if (customer == null) {
            Toast.makeText(this, "Please add a customer first", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.tvCustomerName.text = customer?.name
        binding.tvCurrentOutstanding.text = "Current Outstanding: ${CurrencyFormatter.formatPaiseToRupees(customer?.currentOutstandingPaise ?: 0L)}"

        binding.btnSavePayment.setOnClickListener {
            val amountStr = binding.etAmount.text.toString().trim()
            val ref = binding.etReference.text.toString().trim()

            val amountPaise = CurrencyFormatter.parseRupeesToPaise(amountStr)
            if (amountPaise <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val method = when (binding.rgPaymentMethod.checkedRadioButtonId) {
                binding.rbUpi.id -> "UPI"
                binding.rbBank.id -> "BANK_TRANSFER"
                else -> "CASH"
            }

            val payment = paymentRepo.recordPayment(
                customerId = customer!!.id,
                amountPaise = amountPaise,
                paymentMethod = method,
                referenceNumber = ref,
                createdBy = sessionManager.getUserName(),
                userId = userId
            )

            if (payment != null) {
                Toast.makeText(this, "Payment recorded successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error saving payment", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

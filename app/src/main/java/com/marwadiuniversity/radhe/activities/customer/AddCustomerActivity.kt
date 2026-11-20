package com.marwadiuniversity.radhe.activities.customer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.databinding.ActivityAddCustomerBinding
import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.repositories.CustomerRepository
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.SessionManager

class AddCustomerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCustomerBinding
    private lateinit var customerRepo: CustomerRepository
    private lateinit var sessionManager: SessionManager

    private var editingCustomer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customerRepo = CustomerRepository(this)
        sessionManager = SessionManager(this)

        editingCustomer = intent.getSerializableExtra("CUSTOMER") as? Customer

        if (editingCustomer != null) {
            binding.tvTitle.text = "Edit Customer"
            binding.etName.setText(editingCustomer!!.name)
            binding.etMobile.setText(editingCustomer!!.mobile)
            binding.etAddress.setText(editingCustomer!!.address)
            binding.etOpeningBalance.setText((editingCustomer!!.openingBalancePaise / 100.0).toString())
            binding.etNotes.setText(editingCustomer!!.notes)
        }

        binding.btnSaveCustomer.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val mobile = binding.etMobile.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val openingStr = binding.etOpeningBalance.text.toString().trim()
            val notes = binding.etNotes.text.toString().trim()

            if (name.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Name and mobile number are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = sessionManager.getUserId()
            val openingPaise = CurrencyFormatter.parseRupeesToPaise(openingStr)

            if (editingCustomer != null) {
                val updated = editingCustomer!!.copy(
                    name = name,
                    mobile = mobile,
                    address = address,
                    notes = notes
                )
                val success = customerRepo.updateCustomer(updated, userId)
                if (success) {
                    Toast.makeText(this, "Customer updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error updating customer", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newCustomer = Customer(
                    name = name,
                    mobile = mobile,
                    address = address,
                    openingBalancePaise = openingPaise,
                    currentOutstandingPaise = openingPaise,
                    notes = notes
                )
                val id = customerRepo.addCustomer(newCustomer, userId)
                if (id > 0) {
                    Toast.makeText(this, "Customer added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error adding customer", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

package com.marwadiuniversity.radhe.activities.udhar

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.marwadiuniversity.radhe.adapters.UdharProductAdapter
import com.marwadiuniversity.radhe.databinding.ActivityAddUdharBinding
import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.models.Product
import com.marwadiuniversity.radhe.models.TransactionItem
import com.marwadiuniversity.radhe.repositories.CustomerRepository
import com.marwadiuniversity.radhe.repositories.ProductRepository
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.SessionManager

class AddUdharActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddUdharBinding
    private lateinit var customerRepo: CustomerRepository
    private lateinit var productRepo: ProductRepository
    private lateinit var sessionManager: SessionManager

    private var customer: Customer? = null
    private var availableProducts: List<Product> = emptyList()
    private val selectedQuantities = mutableMapOf<Long, Int>()
    private lateinit var adapter: UdharProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUdharBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customerRepo = CustomerRepository(this)
        productRepo = ProductRepository(this)
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
            Toast.makeText(this, "Please add a customer first!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.tvSelectedCustomerName.text = "Customer: ${customer?.name} (${customer?.mobile})"
        binding.tvPrevBalance.text = "Prev Bal: ${CurrencyFormatter.formatPaiseToRupees(customer?.currentOutstandingPaise ?: 0L)}"

        setupRecyclerView()
        setupListeners()
        loadProducts()
        recalculateTotals()
    }

    private fun setupRecyclerView() {
        adapter = UdharProductAdapter(
            list = emptyList(),
            quantities = selectedQuantities,
            onQuantityChanged = { recalculateTotals() }
        )
        binding.rvUdharProducts.layoutManager = LinearLayoutManager(this)
        binding.rvUdharProducts.adapter = adapter
    }

    private fun setupListeners() {
        binding.etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadProducts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etCustomAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recalculateTotals()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnProceedReview.setOnClickListener {
            val itemsToBuy = mutableListOf<TransactionItem>()
            for ((pId, qty) in selectedQuantities) {
                if (qty > 0) {
                    val p = availableProducts.find { it.id == pId } ?: continue
                    itemsToBuy.add(
                        TransactionItem(
                            productId = p.id,
                            productName = p.name,
                            quantity = qty,
                            unitPricePaise = p.sellingPricePaise,
                            totalPricePaise = p.sellingPricePaise * qty
                        )
                    )
                }
            }

            val customStr = binding.etCustomAmount.text.toString().trim()
            val customPaise = CurrencyFormatter.parseRupeesToPaise(customStr)

            if (customPaise > 0) {
                itemsToBuy.add(
                    TransactionItem(
                        productId = 0,
                        productName = "Custom Amount Entry",
                        quantity = 1,
                        unitPricePaise = customPaise,
                        totalPricePaise = customPaise
                    )
                )
            }

            if (itemsToBuy.isEmpty()) {
                Toast.makeText(this, "Select at least 1 item or enter a custom amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, BillReviewActivity::class.java).apply {
                putExtra("CUSTOMER_ID", customer?.id)
                putExtra("ITEMS", ArrayList(itemsToBuy))
            }
            startActivity(intent)
        }
    }

    private fun loadProducts(query: String = "") {
        val userId = sessionManager.getUserId()
        availableProducts = productRepo.getAllProducts(query, userId = userId)
        adapter.updateList(availableProducts)
    }

    private fun recalculateTotals() {
        var totalQty = 0
        var totalPaise = 0L

        for ((pId, qty) in selectedQuantities) {
            if (qty > 0) {
                val p = availableProducts.find { it.id == pId }
                if (p != null) {
                    totalQty += qty
                    totalPaise += (p.sellingPricePaise * qty)
                }
            }
        }

        val customStr = binding.etCustomAmount.text.toString().trim()
        val customPaise = CurrencyFormatter.parseRupeesToPaise(customStr)
        if (customPaise > 0) {
            totalPaise += customPaise
            totalQty += 1
        }

        val prevBal = customer?.currentOutstandingPaise ?: 0L
        val newOutstanding = prevBal + totalPaise

        binding.tvTotalItems.text = "$totalQty Items Selected"
        binding.tvSubtotal.text = "Subtotal: ${CurrencyFormatter.formatPaiseToRupees(totalPaise)}"
        binding.tvNewOutstanding.text = "New Outstanding: ${CurrencyFormatter.formatPaiseToRupees(newOutstanding)}"
    }
}

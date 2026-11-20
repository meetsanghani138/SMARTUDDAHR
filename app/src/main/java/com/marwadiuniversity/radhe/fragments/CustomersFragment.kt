package com.marwadiuniversity.radhe.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.marwadiuniversity.radhe.activities.customer.AddCustomerActivity
import com.marwadiuniversity.radhe.activities.customer.CustomerDetailsActivity
import com.marwadiuniversity.radhe.adapters.CustomerAdapter
import com.marwadiuniversity.radhe.databinding.FragmentCustomersBinding
import com.marwadiuniversity.radhe.repositories.CustomerRepository
import com.marwadiuniversity.radhe.utils.SessionManager
import com.marwadiuniversity.radhe.utils.WhatsAppHelper

class CustomersFragment : Fragment() {
    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!

    private lateinit var customerRepo: CustomerRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: CustomerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        customerRepo = CustomerRepository(ctx)
        sessionManager = SessionManager(ctx)

        setupRecyclerView()
        setupListeners()
        loadCustomers()
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter(
            list = emptyList(),
            onItemClick = { customer ->
                val intent = Intent(requireContext(), CustomerDetailsActivity::class.java).apply {
                    putExtra("CUSTOMER", customer)
                }
                startActivity(intent)
            },
            onWhatsAppClick = { customer ->
                WhatsAppHelper.sendWhatsAppReminder(
                    context = requireContext(),
                    customerId = customer.id,
                    customerName = customer.name,
                    customerMobile = customer.mobile,
                    shopName = sessionManager.getShopName(),
                    outstandingPaise = customer.currentOutstandingPaise,
                    language = sessionManager.getLanguage()
                )
            }
        )
        binding.rvCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomers.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddCustomer.setOnClickListener {
            startActivity(Intent(requireContext(), AddCustomerActivity::class.java))
        }

        binding.etSearchCustomer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadCustomers(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadCustomers(query: String = "") {
        val userId = sessionManager.getUserId()
        val customers = customerRepo.getAllCustomers(query, userId = userId)
        adapter.updateList(customers)
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            loadCustomers(binding.etSearchCustomer.text.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

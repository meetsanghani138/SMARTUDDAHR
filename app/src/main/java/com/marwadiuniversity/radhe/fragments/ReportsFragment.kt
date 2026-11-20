package com.marwadiuniversity.radhe.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.marwadiuniversity.radhe.activities.customer.CustomerDetailsActivity
import com.marwadiuniversity.radhe.adapters.CustomerAdapter
import com.marwadiuniversity.radhe.databinding.FragmentReportsBinding
import com.marwadiuniversity.radhe.repositories.ReportRepository
import com.marwadiuniversity.radhe.utils.SessionManager
import com.marwadiuniversity.radhe.utils.WhatsAppHelper

class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportRepo: ReportRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var customerAdapter: CustomerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        reportRepo = ReportRepository(ctx)
        sessionManager = SessionManager(ctx)

        setupRecyclerView()
        loadReports()
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter(
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
        binding.rvTopCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopCustomers.adapter = customerAdapter
    }

    private fun loadReports() {
        val userId = sessionManager.getUserId()
        val chartData = reportRepo.getDailySalesData(userId)
        binding.barChartSales.setData(chartData)

        val topCustomers = reportRepo.getTopOutstandingCustomers(5, userId)
        customerAdapter.updateList(topCustomers)
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            loadReports()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.marwadiuniversity.radhe.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.marwadiuniversity.radhe.activities.assistant.AIAssistantActivity
import com.marwadiuniversity.radhe.activities.assistant.VoicePurchaseActivity
import com.marwadiuniversity.radhe.activities.payment.ReceivePaymentActivity
import com.marwadiuniversity.radhe.activities.transaction.TransactionDetailsActivity
import com.marwadiuniversity.radhe.activities.udhar.AddUdharActivity
import com.marwadiuniversity.radhe.adapters.TransactionAdapter
import com.marwadiuniversity.radhe.databinding.FragmentHomeBinding
import com.marwadiuniversity.radhe.repositories.ReportRepository
import com.marwadiuniversity.radhe.repositories.TransactionRepository
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.SessionManager

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportRepo: ReportRepository
    private lateinit var txnRepo: TransactionRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        reportRepo = ReportRepository(ctx)
        txnRepo = TransactionRepository(ctx)
        sessionManager = SessionManager(ctx)

        setupRecyclerView()
        setupListeners()
        loadDashboardData()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList()) { txn ->
            val intent = Intent(requireContext(), TransactionDetailsActivity::class.java).apply {
                putExtra("TRANSACTION_ID", txn.id)
            }
            startActivity(intent)
        }
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentTransactions.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadDashboardData()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnQuickAddUdhar.setOnClickListener {
            startActivity(Intent(requireContext(), AddUdharActivity::class.java))
        }

        binding.btnQuickReceivePayment.setOnClickListener {
            startActivity(Intent(requireContext(), ReceivePaymentActivity::class.java))
        }

        binding.btnVoiceEntry.setOnClickListener {
            startActivity(Intent(requireContext(), VoicePurchaseActivity::class.java))
        }

        binding.btnAiAssistant.setOnClickListener {
            startActivity(Intent(requireContext(), AIAssistantActivity::class.java))
        }
    }

    private fun loadDashboardData() {
        val userId = sessionManager.getUserId()
        val shopName = sessionManager.getShopName()
        binding.tvShopName.text = if (shopName.isNotBlank()) shopName else "My Shop"
        binding.tvOwnerName.text = "Namaste, ${sessionManager.getUserName()}"

        val summary = reportRepo.getDashboardSummary(userId)
        binding.tvTotalUdhar.text = CurrencyFormatter.formatPaiseToRupees(summary.totalUdharPaise)
        binding.tvTodaysSales.text = CurrencyFormatter.formatPaiseToRupees(summary.todaysSalesPaise)
        binding.tvPaymentsReceived.text = CurrencyFormatter.formatPaiseToRupees(summary.paymentsReceivedPaise)

        val recentTxns = txnRepo.getAllTransactions(userId = userId).take(10)
        adapter.updateList(recentTxns)
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            loadDashboardData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

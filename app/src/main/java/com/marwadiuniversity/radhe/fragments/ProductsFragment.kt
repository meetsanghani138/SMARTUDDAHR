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
import com.marwadiuniversity.radhe.activities.product.AddProductActivity
import com.marwadiuniversity.radhe.adapters.ProductAdapter
import com.marwadiuniversity.radhe.databinding.FragmentProductsBinding
import com.marwadiuniversity.radhe.repositories.ProductRepository
import com.marwadiuniversity.radhe.utils.SessionManager

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productRepo: ProductRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        productRepo = ProductRepository(ctx)
        sessionManager = SessionManager(ctx)

        setupRecyclerView()
        setupListeners()
        loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            list = emptyList(),
            onItemClick = { product ->
                val intent = Intent(requireContext(), AddProductActivity::class.java).apply {
                    putExtra("PRODUCT", product)
                }
                startActivity(intent)
            }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }

        binding.etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadProducts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadProducts(query: String = "") {
        val userId = sessionManager.getUserId()
        val products = productRepo.getAllProducts(query, userId = userId)
        adapter.updateList(products)
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            loadProducts(binding.etSearchProduct.text.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.marwadiuniversity.radhe.activities.product

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.marwadiuniversity.radhe.databinding.ActivityAddProductBinding
import com.marwadiuniversity.radhe.models.Category
import com.marwadiuniversity.radhe.models.Product
import com.marwadiuniversity.radhe.repositories.ProductRepository
import com.marwadiuniversity.radhe.utils.CurrencyFormatter
import com.marwadiuniversity.radhe.utils.SessionManager

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var productRepo: ProductRepository
    private lateinit var sessionManager: SessionManager

    private var categories: List<Category> = emptyList()
    private var editingProduct: Product? = null
    private var selectedImagePath: String = ""

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImagePath = uri.toString()
            binding.ivProductPreview.setPadding(0, 0, 0, 0)
            binding.ivProductPreview.imageTintList = null
            Glide.with(this).load(uri).into(binding.ivProductPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productRepo = ProductRepository(this)
        sessionManager = SessionManager(this)

        editingProduct = intent.getSerializableExtra("PRODUCT") as? Product

        setupCategorySpinner()

        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        if (editingProduct != null) {
            binding.tvTitle.text = "Edit Product"
            binding.btnDeleteProduct.visibility = View.VISIBLE
            binding.etName.setText(editingProduct!!.name)
            binding.etSku.setText(editingProduct!!.sku)
            binding.etPurchasePrice.setText((editingProduct!!.purchasePricePaise / 100.0).toString())
            binding.etSellingPrice.setText((editingProduct!!.sellingPricePaise / 100.0).toString())
            binding.etStock.setText(editingProduct!!.stockQuantity.toString())

            selectedImagePath = editingProduct!!.imagePath
            if (selectedImagePath.isNotBlank()) {
                binding.ivProductPreview.setPadding(0, 0, 0, 0)
                binding.ivProductPreview.imageTintList = null
                Glide.with(this).load(selectedImagePath).into(binding.ivProductPreview)
            }

            val catIndex = categories.indexOfFirst { it.id == editingProduct!!.categoryId }
            if (catIndex >= 0) {
                binding.spinnerCategory.setSelection(catIndex)
            }
        }

        binding.btnDeleteProduct.setOnClickListener {
            val userId = sessionManager.getUserId()
            AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete ${editingProduct?.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    if (editingProduct != null) {
                        productRepo.deleteProduct(editingProduct!!.id, userId)
                        Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnSaveProduct.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val sku = binding.etSku.text.toString().trim().ifBlank { "SKU" + System.currentTimeMillis().toString().takeLast(6) }
            val purchaseStr = binding.etPurchasePrice.text.toString().trim()
            val sellingStr = binding.etSellingPrice.text.toString().trim()
            val stockStr = binding.etStock.text.toString().trim()

            if (name.isEmpty() || sellingStr.isEmpty()) {
                Toast.makeText(this, "Product name and selling price are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = sessionManager.getUserId()
            val selectedCategoryPos = binding.spinnerCategory.selectedItemPosition
            val categoryId = if (categories.isNotEmpty() && selectedCategoryPos in categories.indices) {
                categories[selectedCategoryPos].id
            } else 1L

            val purchasePaise = CurrencyFormatter.parseRupeesToPaise(purchaseStr)
            val sellingPaise = CurrencyFormatter.parseRupeesToPaise(sellingStr)
            val stock = stockStr.toIntOrNull() ?: 50

            if (editingProduct != null) {
                val updated = editingProduct!!.copy(
                    sku = sku,
                    name = name,
                    categoryId = categoryId,
                    purchasePricePaise = purchasePaise,
                    sellingPricePaise = sellingPaise,
                    stockQuantity = stock,
                    imagePath = selectedImagePath
                )
                val success = productRepo.updateProduct(updated, userId)
                if (success) {
                    Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error updating product", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newProduct = Product(
                    sku = sku,
                    name = name,
                    categoryId = categoryId,
                    purchasePricePaise = purchasePaise,
                    sellingPricePaise = sellingPaise,
                    stockQuantity = stock,
                    imagePath = selectedImagePath
                )
                val id = productRepo.addProduct(newProduct, userId)
                if (id > 0) {
                    Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error adding product (SKU may exist)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupCategorySpinner() {
        categories = productRepo.getCategories()
        val catNames = categories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, catNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }
}

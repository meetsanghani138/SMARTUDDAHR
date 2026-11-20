package com.marwadiuniversity.radhe.activities.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.MainActivity
import com.marwadiuniversity.radhe.databinding.ActivityShopSetupBinding
import com.marwadiuniversity.radhe.repositories.AuthRepository
import com.marwadiuniversity.radhe.utils.SessionManager

class ShopSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopSetupBinding
    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepo = AuthRepository(this)
        sessionManager = SessionManager(this)

        val userId = sessionManager.getUserId()
        val currentShop = authRepo.getShopProfile(userId)
        binding.etShopName.setText(currentShop.shopName)
        binding.etOwnerName.setText(currentShop.ownerName)
        binding.etShopPhone.setText(currentShop.phone)
        binding.etShopAddress.setText(currentShop.address)

        binding.btnSaveShop.setOnClickListener {
            val name = binding.etShopName.text.toString().trim()
            val owner = binding.etOwnerName.text.toString().trim()
            val phone = binding.etShopPhone.text.toString().trim()
            val address = binding.etShopAddress.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Shop name and phone are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authRepo.updateShopProfile(userId, name, owner, phone, address)
            sessionManager.updateShopDetails(name, phone, address)

            Toast.makeText(this, "Shop Profile Saved!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

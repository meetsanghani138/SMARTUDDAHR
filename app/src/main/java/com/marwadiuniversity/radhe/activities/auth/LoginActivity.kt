package com.marwadiuniversity.radhe.activities.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.MainActivity
import com.marwadiuniversity.radhe.databinding.ActivityLoginBinding
import com.marwadiuniversity.radhe.repositories.AuthRepository
import com.marwadiuniversity.radhe.utils.SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepo = AuthRepository(this)
        sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = authRepo.login(username, password)
            if (user != null) {
                val shop = authRepo.getShopProfile(user.id)
                sessionManager.saveUserSession(
                    userId = user.id,
                    userName = user.fullName,
                    role = user.role,
                    shopId = shop.id,
                    shopName = shop.shopName,
                    shopPhone = shop.phone,
                    shopAddress = shop.address
                )
                Toast.makeText(this, "Welcome ${user.fullName}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials. Try demo: admin / password123", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        binding.tvQuickDemo.setOnClickListener {
            binding.etUsername.setText("admin")
            binding.etPassword.setText("password123")
        }
    }
}

package com.marwadiuniversity.radhe.activities.assistant

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.activities.udhar.AddUdharActivity
import com.marwadiuniversity.radhe.databinding.ActivityVoicePurchaseBinding
import com.marwadiuniversity.radhe.repositories.ProductRepository
import com.marwadiuniversity.radhe.utils.VoicePurchaseParser
import java.util.Locale

class VoicePurchaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVoicePurchaseBinding
    private lateinit var productRepo: ProductRepository

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            binding.tvSpokenText.text = "Spoken: \"$spoken\""

            val products = productRepo.getAllProducts()
            val parsedItems = VoicePurchaseParser.parseSpokenText(spoken, products)

            if (parsedItems.isNotEmpty()) {
                val summary = parsedItems.joinToString("\n• ") { "${it.product.name} x ${it.quantity}" }
                binding.tvVoiceStatus.text = "Recognized items:\n• $summary"
            } else {
                binding.tvVoiceStatus.text = "No matching products found in spoken sentence."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoicePurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productRepo = ProductRepository(this)

        binding.btnMic.setOnClickListener {
            launchVoiceInput()
        }

        binding.btnCreateUdhar.setOnClickListener {
            startActivity(Intent(this, AddUdharActivity::class.java))
            finish()
        }
    }

    private fun launchVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak items to add to Udhar...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech Recognition not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }
}

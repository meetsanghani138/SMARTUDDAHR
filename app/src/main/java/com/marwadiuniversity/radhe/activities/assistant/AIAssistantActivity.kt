package com.marwadiuniversity.radhe.activities.assistant

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.databinding.ActivityAiAssistantBinding
import com.marwadiuniversity.radhe.utils.AIAssistantEngine

class AIAssistantActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiAssistantBinding
    private lateinit var aiEngine: AIAssistantEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        aiEngine = AIAssistantEngine(this)

        binding.btnAsk.setOnClickListener {
            val q = binding.etQuery.text.toString().trim()
            if (q.isEmpty()) {
                Toast.makeText(this, "Enter a question", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ans = aiEngine.answerQuery(q)
            binding.tvAiTitle.text = ans.title
            val fullText = ans.text + if (ans.detailsList.isNotEmpty()) "\n\n" + ans.detailsList.joinToString("\n• ") else ""
            binding.tvAiText.text = fullText
            binding.etQuery.setText("")
        }
    }
}

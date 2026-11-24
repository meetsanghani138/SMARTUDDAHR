package com.marwadiuniversity.radhe.activities

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.R
import com.marwadiuniversity.radhe.databinding.ActivityDeveloperCreditsBinding

class DeveloperCreditsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeveloperCreditsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeveloperCreditsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Default condition: Main developer name is GONE, only Co-Developer is visible
        binding.layoutMainDeveloper.visibility = View.GONE

        // When clicking About Developers: Reveals Main Developer MEET SANGHANI
        binding.btnAboutDevelopers.setOnClickListener {
            if (binding.layoutMainDeveloper.visibility == View.GONE) {
                binding.layoutMainDeveloper.visibility = View.VISIBLE
                val anim = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in)
                binding.layoutMainDeveloper.startAnimation(anim)
            }
        }
    }
}

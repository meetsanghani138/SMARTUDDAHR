package com.marwadiuniversity.radhe.activities.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.MainActivity
import com.marwadiuniversity.radhe.R
import com.marwadiuniversity.radhe.databinding.ActivitySplashBinding
import com.marwadiuniversity.radhe.utils.SessionManager

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Load and apply entrance animations
        val zoomInAnim = AnimationUtils.loadAnimation(this, R.anim.splash_zoom_in)
        val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in)

        binding.cardIcon.startAnimation(zoomInAnim)
        binding.tvAppName.startAnimation(fadeInAnim)
        binding.tvTagline.startAnimation(fadeInAnim)
        binding.tvDeveloperCredit.startAnimation(fadeInAnim)

        // Hold splash screen for 2.5 seconds then navigate
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2500)
    }
}

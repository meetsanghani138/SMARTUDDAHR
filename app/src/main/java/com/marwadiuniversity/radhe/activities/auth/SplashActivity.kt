package com.marwadiuniversity.radhe.activities.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.radhe.MainActivity
import com.marwadiuniversity.radhe.R
import com.marwadiuniversity.radhe.databinding.ActivitySplashBinding
import com.marwadiuniversity.radhe.utils.SessionManager

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    private var tapCount = 0
    private var lastTapTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var navigateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Ensure secret developer credit is hidden initially during normal splash display
        binding.tvDeveloperCredit.visibility = View.GONE

        // Load entrance animations
        val zoomInAnim = AnimationUtils.loadAnimation(this, R.anim.splash_zoom_in)
        val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in)

        binding.cardIcon.startAnimation(zoomInAnim)
        binding.tvAppName.startAnimation(fadeInAnim)
        binding.tvTagline.startAnimation(fadeInAnim)

        // Secret 3-Tap Detection on App Icon
        val secretTapListener = View.OnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTapTime < 800) {
                tapCount++
            } else {
                tapCount = 1
            }
            lastTapTime = currentTime

            if (tapCount >= 2) {
                tapCount = 0
                revealSecretDeveloperCredit()
            }
        }

        binding.cardIcon.setOnClickListener(secretTapListener)
        binding.ivAppIcon.setOnClickListener(secretTapListener)

        // Standard splash delay to proceed automatically
        scheduleNavigation(2500)
    }

    private fun revealSecretDeveloperCredit() {
        // Cancel pending navigation while secret credit is shown
        navigateRunnable?.let { handler.removeCallbacks(it) }

        // Reveal Developer: Sanghani Meet with high-contrast bold font & smooth fade-in
        binding.tvDeveloperCredit.visibility = View.VISIBLE
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in)
        binding.tvDeveloperCredit.startAnimation(fadeIn)

        // Hide automatically after exactly 3 seconds (3000ms), then navigate
        handler.postDelayed({
            binding.tvDeveloperCredit.visibility = View.GONE
            scheduleNavigation(500)
        }, 3000)
    }

    private fun scheduleNavigation(delayMillis: Long) {
        navigateRunnable = Runnable {
            if (sessionManager.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }
        handler.postDelayed(navigateRunnable!!, delayMillis)
    }

    override fun onDestroy() {
        super.onDestroy()
        navigateRunnable?.let { handler.removeCallbacks(it) }
    }
}

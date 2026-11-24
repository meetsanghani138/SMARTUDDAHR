package com.marwadiuniversity.radhe.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageButton
import com.marwadiuniversity.radhe.R

/**
 * Utility to secretly reveal developer attribution:
 * 1. Double tap anywhere on any screen/view secretly shows a clean footer bar:
 *    "Developed by Meet Sanghani" with a close button and smooth slide-up animation.
 */
object DeveloperSecretHelper {

    private const val BANNER_TAG = "SECRET_DEVELOPER_BANNER_TAG"

    /**
     * Attaches secret double-tap detection to an Activity's root decor view.
     */
    fun attachDoubleTapToActivity(activity: Activity) {
        val decorView = activity.window?.decorView ?: return
        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                showSecretDeveloperBanner(activity)
                return true
            }
        })

        decorView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    /**
     * Displays a full-width footer banner:
     * "Developed by Meet Sanghani" with a dedicated close button.
     */
    fun showSecretDeveloperBanner(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return

        val decorView = activity.window?.decorView as? ViewGroup ?: return

        // Remove existing banner if already visible
        val existingView = decorView.findViewWithTag<View>(BANNER_TAG)
        if (existingView != null) {
            decorView.removeView(existingView)
        }

        val inflater = LayoutInflater.from(activity)
        val footerView = inflater.inflate(R.layout.layout_secret_developer_footer, decorView, false)
        footerView.tag = BANNER_TAG

        val btnClose = footerView.findViewById<ImageButton>(R.id.btnCloseSecretBanner)
        btnClose?.setOnClickListener {
            try {
                if (footerView.parent != null) {
                    decorView.removeView(footerView)
                }
            } catch (_: Exception) {
            }
        }

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
        }

        decorView.addView(footerView, params)
        val slideUp = AnimationUtils.loadAnimation(activity, R.anim.splash_fade_in)
        footerView.startAnimation(slideUp)

        // Auto-dismiss after 6 seconds if not closed manually
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (footerView.parent != null) {
                    decorView.removeView(footerView)
                }
            } catch (_: Exception) {
            }
        }, 6000)
    }
}

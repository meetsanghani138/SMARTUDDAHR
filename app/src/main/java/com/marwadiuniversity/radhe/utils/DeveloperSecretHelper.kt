package com.marwadiuniversity.radhe.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.marwadiuniversity.radhe.R

/**
 * Utility to secretly reveal developer attribution:
 * 1. Double tap anywhere on any screen/view secretly shows a clean overlay badge: "Developed by Meet Sanghani"
 * 2. Developer Credits Dialog:
 *    - Default view: Shows Co-Developer DHRUV, Guided By Prof. Jiger Dave, University Marwadi University
 *    - When clicking "About Developers": Reveals Main Developer: MEET SANGHANI
 */
object DeveloperSecretHelper {

    private const val BANNER_TAG = "SECRET_DEVELOPER_BANNER_TAG"

    /**
     * Attaches secret double-tap detection to an Activity's root decor view.
     * When double tapped anywhere, it smoothly presents an overlay badge:
     * "Developed by Meet Sanghani" for 2.5 seconds.
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
     * Attaches secret double tap directly to any View / Fragment root view.
     */
    fun attachDoubleTapToView(view: View, activity: Activity) {
        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                showSecretDeveloperBanner(activity)
                return true
            }
        })
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    /**
     * Displays an animated, high-contrast overlay banner:
     * "Developed by Meet Sanghani"
     */
    fun showSecretDeveloperBanner(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return

        val decorView = activity.window?.decorView as? ViewGroup ?: return

        // Remove existing banner if already visible
        val existingView = decorView.findViewWithTag<View>(BANNER_TAG)
        if (existingView != null) {
            decorView.removeView(existingView)
        }

        val context = activity
        val banner = TextView(context).apply {
            tag = BANNER_TAG
            text = "Developed by Meet Sanghani"
            setTextColor(ContextCompat.getColor(context, R.color.white))
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.04f
            gravity = android.view.Gravity.CENTER

            // Rounded badge background using primary green
            val shape = android.graphics.drawable.GradientDrawable().apply {
                setColor(ContextCompat.getColor(context, R.color.primary_green))
                cornerRadius = 28f
                setStroke(2, ContextCompat.getColor(context, R.color.white))
            }
            background = shape
            setPadding(36, 16, 36, 16)
            elevation = 16f
        }

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            bottomMargin = 100
        }

        decorView.addView(banner, params)
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.splash_fade_in)
        banner.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (banner.parent != null) {
                    decorView.removeView(banner)
                }
            } catch (_: Exception) {
            }
        }, 2500)
    }

    /**
     * Shows Developer Credits dialog with:
     * - Default condition: Co-Developer DHRUV, Guided By Prof. Jiger Dave, University Marwadi University visible
     * - Main Developer MEET SANGHANI is hidden by default
     * - Clicking "About Developers" reveals Main Developer: MEET SANGHANI with a smooth fade-in animation
     */
    fun showDeveloperCreditsDialog(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return

        try {
            val dialogView = activity.layoutInflater.inflate(R.layout.dialog_developer_credits, null)
            val btnAboutDevs = dialogView.findViewById<View>(R.id.btnAboutDevelopers)
            val layoutMainDev = dialogView.findViewById<LinearLayout>(R.id.layoutMainDeveloper)

            // By default condition, Main Developer is GONE
            layoutMainDev?.visibility = View.GONE

            btnAboutDevs?.setOnClickListener {
                if (layoutMainDev != null) {
                    layoutMainDev.visibility = View.VISIBLE
                    val fadeIn = AnimationUtils.loadAnimation(activity, R.anim.splash_fade_in)
                    layoutMainDev.startAnimation(fadeIn)
                }
            }

            AlertDialog.Builder(activity)
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show()
        } catch (_: Exception) {
            // Fallback alert dialog
            var showingMainDev = false
            var dialog: AlertDialog? = null

            val getMessage = { showMain: Boolean ->
                if (showMain) {
                    """
                    👨‍💻 Main Developer: MEET SANGHANI
                    🤝 Co-Developer: DHRUV

                    🎓 Guided By: Prof. Jiger Dave

                    🏛️ University: Marwadi University
                    """.trimIndent()
                } else {
                    """
                    🤝 Co-Developer: DHRUV

                    🎓 Guided By: Prof. Jiger Dave

                    🏛️ University: Marwadi University
                    """.trimIndent()
                }
            }

            val builder = AlertDialog.Builder(activity)
                .setTitle("Developer Credits")
                .setMessage(getMessage(false))
                .setPositiveButton("OK", null)
                .setNeutralButton("About Developers", null)

            dialog = builder.create()
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
                showingMainDev = true
                dialog.setMessage(getMessage(true))
            }
        }
    }
}

package com.marwadiuniversity.radhe

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Window
import com.marwadiuniversity.radhe.utils.DeveloperSecretHelper

class SmartUdharApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Hook window touch callback for secret double tap across every activity/page
                val window = activity.window ?: return
                val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        DeveloperSecretHelper.showSecretDeveloperBanner(activity)
                        return true
                    }
                })

                val originalCallback = window.callback
                if (originalCallback != null) {
                    window.callback = object : Window.Callback by originalCallback {
                        override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
                            if (event != null) {
                                gestureDetector.onTouchEvent(event)
                            }
                            return originalCallback.dispatchTouchEvent(event)
                        }
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}

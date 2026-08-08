package com.example.myapplication.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class LockAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We'll detect screen wake and lock events here later.
    }

    override fun onInterrupt() {
        // Required method.
    }
}
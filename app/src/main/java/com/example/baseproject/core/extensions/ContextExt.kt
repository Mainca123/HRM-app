package com.example.baseproject.core.extensions

import android.content.Context
import android.widget.Toast

/**
 * Extension functions for Context (Toast, etc.).
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

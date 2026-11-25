package com.example.baseproject.core.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast

/**
 * Kotlin Extensions (from AF_ProjectBase).
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

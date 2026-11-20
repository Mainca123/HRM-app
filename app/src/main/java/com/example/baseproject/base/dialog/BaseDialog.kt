package com.example.baseproject.base.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.viewbinding.ViewBinding

/**
 * Base Dialog with ViewBinding support (inspired by AF_ProjectBase).
 */
abstract class BaseDialog<VB : ViewBinding>(context: Context) : Dialog(context) {
    protected lateinit var binding: VB

    abstract fun provideViewBinding(inflater: LayoutInflater): VB

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = provideViewBinding(LayoutInflater.from(context))
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}

package com.example.baseproject.utils

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.example.baseproject.R
import com.google.android.material.button.MaterialButton

enum class DialogTone {
    PRIMARY,
    WARNING,
    DANGER
}

data class DialogActionItem(
    val label: String,
    val isDestructive: Boolean = false,
    val isSelected: Boolean = false,
    val onClick: () -> Unit
)

object ModernDialogHelper {

    /**
     * Shows a unified light-mode rounded confirmation or info dialog.
     */
    fun showConfirm(
        context: Context,
        title: String,
        message: String,
        positiveText: String,
        negativeText: String? = null,
        tone: DialogTone = DialogTone.PRIMARY,
        @DrawableRes iconRes: Int? = null,
        onPositive: (() -> Unit)? = null
    ): AlertDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_modern_confirm, null)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val iconContainer = view.findViewById<FrameLayout>(R.id.iconContainer)
        val ivIcon = view.findViewById<ImageView>(R.id.ivDialogIcon)
        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvDialogMessage)
        val btnNegative = view.findViewById<MaterialButton>(R.id.btnDialogNegative)
        val btnPositive = view.findViewById<MaterialButton>(R.id.btnDialogPositive)

        tvTitle.text = title
        tvMessage.text = message

        val (bgColor, tintColor, btnColor) = when (tone) {
            DialogTone.PRIMARY -> Triple(0xFFEFF6FF.toInt(), 0xFF1E40AF.toInt(), 0xFF1E40AF.toInt())
            DialogTone.WARNING -> Triple(0xFFFEF3C7.toInt(), 0xFFD97706.toInt(), 0xFFD97706.toInt())
            DialogTone.DANGER -> Triple(0xFFFEE2E2.toInt(), 0xFFDC2626.toInt(), 0xFFDC2626.toInt())
        }
        iconContainer.backgroundTintList = ColorStateList.valueOf(bgColor)
        ivIcon.imageTintList = ColorStateList.valueOf(tintColor)
        if (iconRes != null) {
            ivIcon.setImageResource(iconRes)
        } else {
            iconContainer.visibility = View.GONE
        }

        btnPositive.text = positiveText
        btnPositive.backgroundTintList = ColorStateList.valueOf(btnColor)
        btnPositive.setOnClickListener {
            dialog.dismiss()
            onPositive?.invoke()
        }

        if (negativeText.isNullOrBlank()) {
            btnNegative.visibility = View.GONE
        } else {
            btnNegative.visibility = View.VISIBLE
            btnNegative.text = negativeText
            btnNegative.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
        return dialog
    }

    /**
     * Shows a unified light-mode rounded action selector dialog.
     */
    fun showActions(
        context: Context,
        title: String,
        subtitle: String? = null,
        actions: List<DialogActionItem>,
        closeText: String = context.getString(R.string.close)
    ): AlertDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_modern_actions, null)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = view.findViewById<TextView>(R.id.tvActionsTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvActionsSubtitle)
        val container = view.findViewById<LinearLayout>(R.id.layoutActionsContainer)
        val btnClose = view.findViewById<MaterialButton>(R.id.btnActionsClose)

        tvTitle.text = title
        if (!subtitle.isNullOrBlank()) {
            tvSubtitle.visibility = View.VISIBLE
            tvSubtitle.text = subtitle
        }

        val density = context.resources.displayMetrics.density
        val padH = (16 * density).toInt()
        val padV = (14 * density).toInt()
        val marginBottom = (10 * density).toInt()

        actions.forEachIndexed { index, action ->
            val row = TextView(context).apply {
                text = if (action.isSelected) "${action.label}  ✓" else action.label
                textSize = 15f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(padH, padV, padH, padV)
                setBackgroundResource(R.drawable.bg_modern_action_item)
                when {
                    action.isDestructive -> setTextColor(0xFFDC2626.toInt())
                    action.isSelected -> {
                        setTextColor(0xFF1E40AF.toInt())
                        backgroundTintList = ColorStateList.valueOf(0xFFEFF6FF.toInt())
                    }
                    else -> setTextColor(0xFF0F172A.toInt())
                }
                setTypeface(null, if (action.isSelected || action.isDestructive) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index < actions.lastIndex) bottomMargin = marginBottom
                }
                setOnClickListener {
                    dialog.dismiss()
                    action.onClick()
                }
            }
            container.addView(row)
        }

        btnClose.text = closeText
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
        return dialog
    }
}

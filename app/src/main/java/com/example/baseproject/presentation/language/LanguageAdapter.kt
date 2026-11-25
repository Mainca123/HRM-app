package com.example.baseproject.presentation.language

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.baseproject.R
import com.example.baseproject.core.base.BaseAdapter
import com.example.baseproject.domain.model.Language
import com.example.baseproject.databinding.ItemLanguageBinding

class LanguageAdapter : BaseAdapter<Language, ItemLanguageBinding>() {

    override fun provideViewBinding(parent: ViewGroup): ItemLanguageBinding {
        return ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun binData(viewBinding: ItemLanguageBinding, item: Language, position: Int) {
        viewBinding.apply {
            imgLanguage.setImageResource(item.flagResId)
            tvTitle.text = item.nativeName
            tvSubtitle.text = item.translatedName

            if (item.selected) {
                container.setBackgroundResource(R.drawable.bg_language_selected)
                ivCheck.setImageResource(R.drawable.ic_checked)
            } else {
                container.setBackgroundResource(R.drawable.bg_language_normal)
                ivCheck.setImageResource(R.drawable.ic_radio_unchecked)
            }

            root.setOnClickListener {
                onClick?.invoke(item)
            }
        }
    }

    fun selectLanguage(languageCode: String) {
        var index = dataSet.indexOfFirst { it.selected }
        if (index > -1) {
            dataSet[index].selected = false
            notifyItemChanged(index)
        }
        index = dataSet.indexOfFirst { it.languageCode == languageCode }
        if (index > -1) {
            dataSet[index].selected = true
            notifyItemChanged(index)
        }
    }

    fun selectedLanguage(): Language? = dataSet.firstOrNull { it.selected }
}

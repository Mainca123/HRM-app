package com.example.baseproject.presentation.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.baseproject.core.base.BaseAdapter
import com.example.baseproject.domain.model.OnBoarding
import com.example.baseproject.databinding.ItemOnboardingBinding

class OnBoardingAdapter : BaseAdapter<OnBoarding, ItemOnboardingBinding>() {

    override fun provideViewBinding(parent: ViewGroup): ItemOnboardingBinding {
        return ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun binData(viewBinding: ItemOnboardingBinding, item: OnBoarding, position: Int) {
        viewBinding.apply {
            clOnboarding.isVisible = true
            imgBoarding.setImageResource(item.imageId)
            tvTitle.text = root.context.resources.getString(item.title)
            tvContent.text = root.context.resources.getString(item.content)
        }
    }
}

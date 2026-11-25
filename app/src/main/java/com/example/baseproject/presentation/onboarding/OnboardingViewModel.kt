package com.example.baseproject.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.domain.model.OnBoarding
import com.example.baseproject.domain.usecase.GetListOnBoardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(private val getListOnBoardingUseCase: GetListOnBoardingUseCase) : ViewModel() {

    private val listOnBoardingFlow = MutableStateFlow<List<OnBoarding>>(emptyList())
    var listOnBoarding = listOnBoardingFlow.asStateFlow()
    var currentPosition = 0

    fun getListOnBoarding() {
        viewModelScope.launch {
            listOnBoardingFlow.value = getListOnBoardingUseCase.execute(GetListOnBoardingUseCase.Param())
        }
    }
}

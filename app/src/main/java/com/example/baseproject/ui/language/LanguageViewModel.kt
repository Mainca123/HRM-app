package com.example.baseproject.ui.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.model.Language
import com.example.baseproject.data.usecase.GetListLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val getListLanguageUseCase: GetListLanguageUseCase
) : ViewModel() {
    private val listLanguageFlow = MutableStateFlow<List<Language>>(emptyList())
    val listLanguage = listLanguageFlow.asStateFlow()

    fun loadListLanguage() {
        viewModelScope.launch {
            listLanguageFlow.value = getListLanguageUseCase.execute(GetListLanguageUseCase.Param())
        }
    }
}

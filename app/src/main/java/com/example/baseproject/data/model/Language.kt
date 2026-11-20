package com.example.baseproject.data.model

data class Language(
    val languageCode: String,
    val flagResId: Int,
    val nativeName: String,
    val translatedName: String,
    var selected: Boolean = false
)

package com.example.baseproject.data.repository

import com.example.baseproject.data.local.LocalDataSource
import com.example.baseproject.domain.model.Language
import com.example.baseproject.domain.model.OnBoarding
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val localDataSource: LocalDataSource
) {
    fun getListLanguage(): List<Language> = localDataSource.getListLanguage()

    fun getListOnBoarding(): List<OnBoarding> = localDataSource.getListOnBoarding()
}

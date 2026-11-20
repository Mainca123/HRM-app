package com.example.baseproject.data.repositories

import com.example.baseproject.data.database.table.TableTest
import com.example.baseproject.data.model.Language
import com.example.baseproject.data.model.OnBoarding
import com.example.baseproject.data.sources.LocalDataSource
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val localDataSource: LocalDataSource
) {
    fun getListLanguage(): List<Language> {
        return localDataSource.getListLanguage()
    }

    fun getListOnBoarding(): List<OnBoarding> {
        return localDataSource.getListOnBoarding()
    }

    suspend fun insert(tableTest: TableTest) {
        localDataSource.insert(tableTest)
    }

    fun getList() = localDataSource.getList()

    suspend fun delete(tableTest: TableTest) {
        localDataSource.delete(tableTest)
    }
}

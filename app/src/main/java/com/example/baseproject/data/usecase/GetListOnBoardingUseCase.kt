package com.example.baseproject.data.usecase

import com.example.baseproject.data.model.OnBoarding
import com.example.baseproject.data.repositories.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetListOnBoardingUseCase @Inject constructor(private val localRepository: LocalRepository) :
    UseCase<GetListOnBoardingUseCase.Param, List<OnBoarding>>() {

    open class Param : UseCase.Param()

    override suspend fun execute(param: Param): List<OnBoarding> = withContext(Dispatchers.IO) {
        localRepository.getListOnBoarding()
    }
}

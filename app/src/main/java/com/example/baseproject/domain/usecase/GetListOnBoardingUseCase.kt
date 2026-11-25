package com.example.baseproject.domain.usecase

import com.example.baseproject.domain.model.OnBoarding
import com.example.baseproject.data.repository.LocalRepository
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

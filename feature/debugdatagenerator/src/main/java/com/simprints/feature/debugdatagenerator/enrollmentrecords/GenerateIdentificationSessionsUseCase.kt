package com.simprints.feature.debugdatagenerator.enrollmentrecords

import com.simprints.feature.orchestrator.usecases.response.CreateIdentifyResponseUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.matcher.usecases.MatcherUseCase
import java.io.Serializable
import javax.inject.Inject

class GenerateIdentificationSessionsUseCase @Inject constructor(
    private val createIdentifyResponse: CreateIdentifyResponseUseCase,
    private val configRepository: ConfigRepository,
    private val matcher: MatcherUseCase,
) {
    suspend operator fun invoke(sdks: List<BiometricSdk>) = createIdentifyResponse(
        configRepository.getProjectConfiguration(),
        generateIdentificationResults(sdks),
    )

    private fun generateIdentificationResults(sdks: List<BiometricSdk>): List<Serializable> {
        // 1. generate the templates to be used for identification
        // 2. load samples from db
        // 3. run the matcher
        // 4. return the results
    }
}

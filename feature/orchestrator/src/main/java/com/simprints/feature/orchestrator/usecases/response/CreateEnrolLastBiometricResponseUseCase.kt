package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import java.io.Serializable
import javax.inject.Inject

internal class CreateEnrolLastBiometricResponseUseCase @Inject constructor() {
    operator fun invoke(results: List<Serializable>) = results
        .filterIsInstance(EnrolLastBiometricResult::class.java)
        .lastOrNull()
        ?.newSubjectId
        ?.let { AppEnrolResponse(it) }
        ?: AppErrorResponse(AppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED)
}

package com.simprints.feature.orchestrator.usecases.response

import android.os.Parcelable
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.orchestrator.model.responses.AppEnrolResponse
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.moduleapi.app.responses.IAppErrorReason
import javax.inject.Inject

internal class CreateEnrolLastBiometricResponseUseCase @Inject constructor() {

    operator fun invoke(results: List<Parcelable>) = results
        .filterIsInstance(EnrolLastBiometricResult::class.java)
        .lastOrNull()
        ?.newSubjectId
        ?.let { AppEnrolResponse(it) }
        ?: AppErrorResponse(IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED)
}

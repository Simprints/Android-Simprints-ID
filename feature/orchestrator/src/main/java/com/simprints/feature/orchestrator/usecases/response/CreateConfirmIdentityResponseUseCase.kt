package com.simprints.feature.orchestrator.usecases.response

import android.os.Parcelable
import com.simprints.feature.orchestrator.model.responses.AppConfirmationResponse
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

internal class CreateConfirmIdentityResponseUseCase @Inject constructor() {

    operator fun invoke(results: List<Parcelable>): IAppResponse = results
        .filterIsInstance(SelectSubjectResult::class.java)
        .lastOrNull()
        ?.let { AppConfirmationResponse(true) }
        ?: AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)
}

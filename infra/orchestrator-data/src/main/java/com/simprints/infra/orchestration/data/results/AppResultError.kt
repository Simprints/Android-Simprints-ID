package com.simprints.infra.orchestration.data.results

import android.os.Parcelable
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppResponseType
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppResultError(
    override val reason: IAppErrorReason,
) : IAppErrorResponse, Parcelable {

    override val type = IAppResponseType.ERROR
}

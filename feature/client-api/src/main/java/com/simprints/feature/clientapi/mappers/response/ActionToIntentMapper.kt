package com.simprints.feature.clientapi.mappers.response

import android.os.Bundle
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.feature.clientapi.models.LibSimprintsConstants
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.infra.orchestration.data.ActionResponse
import javax.inject.Inject

internal class ActionToIntentMapper @Inject constructor(
    private val mapOdkResponse: OdkResponseMapper,
    private val mapCommCareResponse: CommCareResponseMapper,
    private val mapLibSimprintsResponse: LibSimprintsResponseMapper,
) {
    operator fun invoke(response: ActionResponse): Bundle = when (response.actionIdentifier.packageName) {
        OdkConstants.PACKAGE_NAME -> mapOdkResponse(response)
        CommCareConstants.PACKAGE_NAME -> mapCommCareResponse(response)
        LibSimprintsConstants.PACKAGE_NAME -> mapLibSimprintsResponse(response)
        else -> throw InvalidRequestException("Unknown package name", ClientApiError.INVALID_STATE_FOR_INTENT_ACTION)
    }
}

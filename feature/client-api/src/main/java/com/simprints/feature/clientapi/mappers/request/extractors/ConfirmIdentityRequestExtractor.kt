package com.simprints.feature.clientapi.mappers.request.extractors

import com.simprints.feature.clientapi.extensions.extractString
import com.simprints.libsimprints.Constants.SIMPRINTS_SELECTED_GUID
import com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID

internal class ConfirmIdentityRequestExtractor(
    val extras: Map<String, Any>,
) : ActionRequestExtractor(extras) {
    fun getSessionId(): String = extras.extractString(SIMPRINTS_SESSION_ID)

    fun getSelectedGuid(): String = extras.extractString(SIMPRINTS_SELECTED_GUID)

    override val expectedKeys = super.keys + listOf(SIMPRINTS_SESSION_ID, SIMPRINTS_SELECTED_GUID)
}

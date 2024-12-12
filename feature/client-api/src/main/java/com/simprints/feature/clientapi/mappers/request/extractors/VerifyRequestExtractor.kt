package com.simprints.feature.clientapi.mappers.request.extractors

import com.simprints.feature.clientapi.extensions.extractString
import com.simprints.libsimprints.Constants

internal open class VerifyRequestExtractor(
    val extras: Map<String, Any>,
) : ActionRequestExtractor(extras) {
    fun getVerifyGuid(): String = extras.extractString(Constants.SIMPRINTS_VERIFY_GUID)

    override val expectedKeys = super.keys + listOf(Constants.SIMPRINTS_VERIFY_GUID)
}

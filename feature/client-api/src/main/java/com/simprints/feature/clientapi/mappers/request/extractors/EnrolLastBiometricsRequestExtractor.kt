package com.simprints.feature.clientapi.mappers.request.extractors

import com.simprints.feature.clientapi.extensions.extractString
import com.simprints.libsimprints.Constants

internal class EnrolLastBiometricsRequestExtractor(
    val extras: Map<String, Any>,
) : ActionRequestExtractor(extras) {
    fun getSessionId(): String = extras.extractString(Constants.SIMPRINTS_SESSION_ID)

    override val expectedKeys = super.keys + listOf(Constants.SIMPRINTS_SESSION_ID)
}

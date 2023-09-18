package com.simprints.id.orchestrator.steps.core.response

import kotlinx.parcelize.Parcelize

@Parcelize
class FetchGUIDResponse(
    val isGuidFound: Boolean,
    val wasOnline: Boolean = false
): CoreResponse(type = CoreResponseType.FETCH_GUID)

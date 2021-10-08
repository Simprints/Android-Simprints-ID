package com.simprints.id.orchestrator.steps.core.response

import kotlinx.parcelize.Parcelize

@Parcelize
class FetchGUIDResponse(val isGuidFound: Boolean): CoreResponse(type = CoreResponseType.FETCH_GUID)

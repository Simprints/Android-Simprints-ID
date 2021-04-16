package com.simprints.id.orchestrator.steps.core.response

import kotlinx.parcelize.Parcelize

@Parcelize
data class GuidSelectionResponse(val identificationOutcome: Boolean): CoreResponse(type = CoreResponseType.GUID_SELECTION)

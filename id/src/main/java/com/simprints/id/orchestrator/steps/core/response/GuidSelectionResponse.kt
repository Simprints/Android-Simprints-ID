package com.simprints.id.orchestrator.steps.core.response

import kotlinx.android.parcel.Parcelize

@Parcelize
data class GuidSelectionResponse(val identificationOutcome: Boolean): CoreResponse(type = CoreResponseType.GUID_SELECTION)

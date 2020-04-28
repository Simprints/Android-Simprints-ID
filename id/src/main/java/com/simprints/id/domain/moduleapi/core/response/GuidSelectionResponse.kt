package com.simprints.id.domain.moduleapi.core.response

import kotlinx.android.parcel.Parcelize

@Parcelize
data class GuidSelectionResponse(val identificationOutcome: Boolean): CoreResponse(type = CoreResponseType.GUID_SELECTION)

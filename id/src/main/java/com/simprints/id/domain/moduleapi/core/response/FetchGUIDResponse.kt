package com.simprints.id.domain.moduleapi.core.response

import kotlinx.android.parcel.Parcelize

@Parcelize
class FetchGUIDResponse(val isGuidFound: Boolean): CoreResponse(type = CoreResponseType.FETCH_GUID)

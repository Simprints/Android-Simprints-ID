package com.simprints.id.domain.moduleapi.core.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
class FetchGUIDRequest(val projectId: String, val verifyGuid: String): CoreRequest

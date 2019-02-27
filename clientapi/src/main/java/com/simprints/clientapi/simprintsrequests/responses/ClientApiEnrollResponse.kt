package com.simprints.clientapi.simprintsrequests.responses

import kotlinx.android.parcel.Parcelize

@Parcelize
data class ClientApiEnrollResponse(val guid: String) : SimprintsIdResponse


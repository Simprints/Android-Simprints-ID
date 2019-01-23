package com.simprints.clientapi.simprintsrequests.responses

import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrollResponse(val guid: String) : SimprintsIdResponse


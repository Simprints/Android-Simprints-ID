package com.simprints.clientapi.simprintsrequests.responses

import kotlinx.android.parcel.Parcelize


@Parcelize
data class VerifyResponse(val guid: String, val confidence: Int, val tier: Tier) : SimprintsIdResponse

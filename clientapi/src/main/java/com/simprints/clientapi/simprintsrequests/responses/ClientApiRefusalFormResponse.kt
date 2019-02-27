package com.simprints.clientapi.simprintsrequests.responses

import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiRefusalFormResponse(val reason: String, val extra: String) : SimprintsIdResponse

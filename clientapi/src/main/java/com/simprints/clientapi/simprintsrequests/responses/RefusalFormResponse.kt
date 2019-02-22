package com.simprints.clientapi.simprintsrequests.responses

import kotlinx.android.parcel.Parcelize


@Parcelize
data class RefusalFormResponse(val reason: String, val extra: String) : SimprintsIdResponse

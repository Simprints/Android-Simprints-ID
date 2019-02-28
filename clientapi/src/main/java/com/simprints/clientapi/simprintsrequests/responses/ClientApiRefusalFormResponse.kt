package com.simprints.clientapi.simprintsrequests.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiRefusalFormResponse
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiRefusalFormResponse(override val reason: String,
                                        override val extra: String) : IClientApiRefusalFormResponse

package com.simprints.clientapi.models.appinterface.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiRefusalFormResponse
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AppRefusalFormResponse(override val reason: String,
                                  override val extra: String) : IClientApiRefusalFormResponse

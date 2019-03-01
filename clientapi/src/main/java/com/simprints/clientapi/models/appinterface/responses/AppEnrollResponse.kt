package com.simprints.clientapi.models.appinterface.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiEnrollResponse
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AppEnrollResponse(override val guid: String) : IClientApiEnrollResponse

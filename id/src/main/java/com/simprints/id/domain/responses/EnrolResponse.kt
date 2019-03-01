package com.simprints.id.domain.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiEnrollResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrolResponse(val guid: String): Response

fun EnrolResponse.toClientApiEnrolResponse(): IClientApiEnrollResponse = ClientApiEnrollResponse(guid)

@Parcelize
private class ClientApiEnrollResponse(override val guid: String): IClientApiEnrollResponse

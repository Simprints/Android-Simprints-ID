package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiEnrollResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppEnrolResponse(val guid: String): AppResponse

fun AppEnrolResponse.toDomainClientApiEnrol() = ClientApiEnrollResponse(guid)

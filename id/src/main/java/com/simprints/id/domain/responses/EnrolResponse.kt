package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiEnrollResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrolResponse(val guid: String): Response

fun EnrolResponse.toDomainClientApiEnrol() = ClientApiEnrollResponse(guid)

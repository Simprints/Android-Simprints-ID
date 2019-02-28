package com.simprints.clientapi.simprintsrequests.responses

import com.simprints.moduleinterfaces.clientapi.responses.ClientEnrollResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ClientApiEnrollResponse(override val guid: String) : ClientEnrollResponse


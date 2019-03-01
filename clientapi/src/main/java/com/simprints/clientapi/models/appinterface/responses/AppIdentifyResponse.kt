package com.simprints.clientapi.models.appinterface.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiIdentifyResponse
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AppIdentifyResponse(override val identifications: List<IClientApiIdentifyResponse.IIdentificationResult>,
                               override val sessionId: String) : IClientApiIdentifyResponse

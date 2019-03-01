package com.simprints.moduleinterfaces.clientapi.responses

import android.os.Parcelable


interface IClientApiIdentifyResponse : IClientApiResponse {

    val identifications: List<IIdentificationResult>
    val sessionId: String

    interface IIdentificationResult : Parcelable {
        val guid: String
        val confidence: Int
        val tier: IClientApiResponseTier
    }
}

package com.simprints.moduleinterfaces.clientapi.responses

import android.os.Parcelable


interface IClientApiIdentifyResponse : IClientApiResponse {

    val identifications: List<Identification>
    val sessionId: String

    interface Identification : Parcelable {
        val guid: String
        val confidence: Int
        val tier: IClientApiResponseTier
    }

}

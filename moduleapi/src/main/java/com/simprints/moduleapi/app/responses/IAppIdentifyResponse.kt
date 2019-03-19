package com.simprints.moduleapi.app.responses

import android.os.Parcelable


interface IAppIdentifyResponse : IAppResponse {

    val identifications: List<IIdentificationResult>
    val sessionId: String

    interface IIdentificationResult : Parcelable {
        val guid: String
        val confidence: Int
        val tier: IAppResponseTier
    }
}

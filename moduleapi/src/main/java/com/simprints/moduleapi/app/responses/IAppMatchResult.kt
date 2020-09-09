package com.simprints.moduleapi.app.responses

import android.os.Parcelable

interface IAppMatchResult : Parcelable {
    val guid: String
    val confidenceScore: Int
    val tier: IAppResponseTier
    val matchConfidence: IAppMatchConfidence
}

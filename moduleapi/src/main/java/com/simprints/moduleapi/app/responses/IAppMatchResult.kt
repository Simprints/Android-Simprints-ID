package com.simprints.moduleapi.app.responses

import android.os.Parcelable

interface IAppMatchResult : Parcelable {
    val guid: String
    val confidence: Int
    val tier: IAppResponseTier
}

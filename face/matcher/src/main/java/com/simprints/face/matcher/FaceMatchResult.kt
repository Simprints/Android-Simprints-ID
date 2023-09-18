package com.simprints.face.matcher

import android.os.Parcelable
import com.simprints.moduleapi.face.responses.IFaceMatchResult
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceMatchResult(
    val result: List<IFaceMatchResult>,
) : Parcelable {

    @Parcelize
    internal data class Item(
        override val guid: String,
        override val confidence: Float,
    ) : IFaceMatchResult, Parcelable
}

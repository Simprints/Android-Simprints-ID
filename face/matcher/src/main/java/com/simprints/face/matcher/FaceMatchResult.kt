package com.simprints.face.matcher

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.moduleapi.face.responses.IFaceMatchResult
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FaceMatchResult(
    val results: List<IFaceMatchResult>,
) : Parcelable {

    @Keep
    @Parcelize
    internal data class Item(
        override val guid: String,
        override val confidence: Float,
    ) : IFaceMatchResult, Parcelable
}

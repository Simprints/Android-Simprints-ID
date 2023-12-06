package com.simprints.matcher

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
interface MatchResult : Parcelable {

    val results: List<MatchResultItem>
}

/**
 * This is required to bridge different interfaces from moduleApi module.
 */
@Keep
interface MatchResultItem : Parcelable {

    val subjectId: String
    val confidence: Float
}

@Keep
@Parcelize
data class FaceMatchResult(
    override val results: List<MatchResultItem>,
) : MatchResult {

    @Keep
    @Parcelize
    data class Item(
        override val subjectId: String,
        override val confidence: Float,
    ) : MatchResultItem
}

@Keep
@Parcelize
data class FingerprintMatchResult(
    override val results: List<MatchResultItem>,
) : MatchResult {

    @Keep
    @Parcelize
    data class Item(
        override val subjectId: String,
        override val confidence: Float,
    ) : MatchResultItem
}

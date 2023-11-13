package com.simprints.matcher

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface MatchResult : Parcelable {

    val results: List<MatchResultItem>
}

/**
 * This is required to bridge different interfaces from moduleApi module.
 */
interface MatchResultItem : Parcelable {

    val subjectId: String
    val confidence: Float
}

@Parcelize
data class FaceMatchResult(
    override val results: List<MatchResultItem>,
) : MatchResult {

    @Parcelize
    data class Item(
        override val subjectId: String,
        override val confidence: Float,
    ) : MatchResultItem
}

@Parcelize
data class FingerprintMatchResult(
    override val results: List<MatchResultItem>,
) : MatchResult {

    @Parcelize
    data class Item(
        override val subjectId: String,
        override val confidence: Float,
    ) : MatchResultItem
}

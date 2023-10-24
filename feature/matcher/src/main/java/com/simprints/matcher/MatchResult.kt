package com.simprints.matcher

import android.os.Parcelable
import com.simprints.moduleapi.face.responses.IFaceMatchResult
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintMatchResult
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
        override val guid: String,
        override val confidence: Float,
    ) : IFaceMatchResult, MatchResultItem {

        override val subjectId: String
            get() = guid
    }
}

@Parcelize
data class FingerprintMatchResult(
    override val results: List<MatchResultItem>,
) : MatchResult {

    @Parcelize
    data class Item(
        override val personId: String,
        override val confidenceScore: Float,
    ) : IFingerprintMatchResult, MatchResultItem {

        override val subjectId: String
            get() = personId

        override val confidence: Float
            get() = confidenceScore
    }
}
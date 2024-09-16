package com.simprints.matcher

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.FingerprintConfiguration
import java.io.Serializable

@Keep
interface MatchResult : Serializable {
    val results: List<MatchResultItem>
}

/**
 * This is required to bridge different interfaces from moduleApi module.
 */
@Keep
interface MatchResultItem : Serializable {
    val subjectId: String
    val confidence: Float
}

@Keep
data class FaceMatchResult(
    override val results: List<MatchResultItem>,
) : MatchResult {

    @Keep
    data class Item(
        override val subjectId: String,
        override val confidence: Float,
    ) : MatchResultItem
}

@Keep
data class FingerprintMatchResult(
    override val results: List<MatchResultItem>,
    val sdk: FingerprintConfiguration.BioSdk,
) : MatchResult {

    @Keep
    data class Item(
        override val subjectId: String,
        override val confidence: Float,
    ) : MatchResultItem
}

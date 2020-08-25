package com.simprints.id.domain.moduleapi.app.responses.entities

import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds

enum class MatchConfidence {
    NONE,
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun computeMatchConfidenceForFingerprint(confidenceScore: Int,
                                                 fingerprintScores: Map<FingerprintConfidenceThresholds, Int>) =
            when {
                confidenceScore < fingerprintScores.getValue(FingerprintConfidenceThresholds.LOW) -> NONE
                confidenceScore < fingerprintScores.getValue(FingerprintConfidenceThresholds.MEDIUM) -> LOW
                confidenceScore < fingerprintScores.getValue(FingerprintConfidenceThresholds.HIGH) -> MEDIUM
                else -> HIGH
            }

        fun computeMatchConfidenceForFace(confidenceScore: Int,
                                          faceScores: Map<FaceConfidenceThresholds, Int>) =
            when {
                confidenceScore < faceScores.getValue(FaceConfidenceThresholds.LOW) -> NONE
                confidenceScore < faceScores.getValue(FaceConfidenceThresholds.MEDIUM) -> LOW
                confidenceScore < faceScores.getValue(FaceConfidenceThresholds.HIGH) -> MEDIUM
                else -> HIGH
            }
    }
}

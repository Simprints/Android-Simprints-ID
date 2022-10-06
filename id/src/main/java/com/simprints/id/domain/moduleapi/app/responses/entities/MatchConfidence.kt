package com.simprints.id.domain.moduleapi.app.responses.entities

import com.simprints.infra.config.domain.models.DecisionPolicy

enum class MatchConfidence {
    NONE,
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun computeMatchConfidence(confidenceScore: Int, policy: DecisionPolicy) =
            when {
                confidenceScore < policy.low -> NONE
                confidenceScore < policy.medium -> LOW
                confidenceScore < policy.high -> MEDIUM
                else -> HIGH
            }
    }
}

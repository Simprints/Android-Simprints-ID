package com.simprints.id.domain.responses

enum class TierResponse {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5;


    fun computeTier(score: Float): TierResponse {
        return if (score < 20f) {
            TIER_5
        } else if (score < 35f) {
            TIER_4
        } else if (score < 50f) {
            TIER_3
        } else if (score < 75f) {
            TIER_2
        } else {
            TIER_1
        }
    }
}


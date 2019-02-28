package com.simprints.id.domain.matching

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponseTier

enum class Tier {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5;

    companion object {
        @JvmStatic fun computeTier(score: Float): Tier {
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
}

fun Tier.toClientApiIClientApiResponseTier(): IClientApiResponseTier =
    when(this) {
        Tier.TIER_1 -> IClientApiResponseTier.TIER_1
        Tier.TIER_2 -> IClientApiResponseTier.TIER_2
        Tier.TIER_3 -> IClientApiResponseTier.TIER_3
        Tier.TIER_4 -> IClientApiResponseTier.TIER_4
        Tier.TIER_5 -> IClientApiResponseTier.TIER_5
    }

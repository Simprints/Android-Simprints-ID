package com.simprints.face.data.moduleapi.face.responses.entities

import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceTier

enum class FaceTier {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5
}

fun FaceTier.toAppTier() =
    when(this) {
        FaceTier.TIER_1 -> Tier.TIER_1
        FaceTier.TIER_2 -> Tier.TIER_2
        FaceTier.TIER_3 -> Tier.TIER_3
        FaceTier.TIER_4 -> Tier.TIER_4
        FaceTier.TIER_5 -> Tier.TIER_5
    }

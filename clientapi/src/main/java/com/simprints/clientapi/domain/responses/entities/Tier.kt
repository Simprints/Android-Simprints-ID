package com.simprints.clientapi.domain.responses.entities

import com.simprints.moduleapi.app.responses.IAppResponseTier

enum class Tier {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5
}

fun IAppResponseTier.fromModuleApiToDomain() = when(this) {
    IAppResponseTier.TIER_1 -> Tier.TIER_1
    IAppResponseTier.TIER_2 -> Tier.TIER_2
    IAppResponseTier.TIER_3 -> Tier.TIER_3
    IAppResponseTier.TIER_4 -> Tier.TIER_4
    IAppResponseTier.TIER_5 -> Tier.TIER_5
}

package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import com.simprints.id.domain.moduleapi.app.responses.entities.Tier

enum class FingerprintTier {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5
}

fun FingerprintTier.toAppTier() =
    when(this) {
        FingerprintTier.TIER_1 -> Tier.TIER_1
        FingerprintTier.TIER_2 -> Tier.TIER_2
        FingerprintTier.TIER_3 -> Tier.TIER_3
        FingerprintTier.TIER_4 -> Tier.TIER_4
        FingerprintTier.TIER_5 -> Tier.TIER_5
    }

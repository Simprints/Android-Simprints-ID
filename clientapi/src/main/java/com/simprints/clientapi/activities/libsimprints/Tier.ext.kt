package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.domain.responses.entities.Tier
import com.simprints.libsimprints.Tier as LibsimprintsTier

fun Tier.fromDomainToLibsimprintsTier() =
    when(this) {
        Tier.TIER_1 -> LibsimprintsTier.TIER_1
        Tier.TIER_2 -> LibsimprintsTier.TIER_2
        Tier.TIER_3 -> LibsimprintsTier.TIER_3
        Tier.TIER_4 -> LibsimprintsTier.TIER_4
        Tier.TIER_5 -> LibsimprintsTier.TIER_5
    }

package com.simprints.id.domain.moduleapi.face.responses.entities

import com.simprints.moduleapi.face.responses.entities.IFaceTier

enum class FaceTier {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5
}

fun IFaceTier.fromDomainApiToDomain(): FaceTier =
    when (this) {
        IFaceTier.TIER_1 -> FaceTier.TIER_1
        IFaceTier.TIER_2 -> FaceTier.TIER_2
        IFaceTier.TIER_3 -> FaceTier.TIER_3
        IFaceTier.TIER_4 -> FaceTier.TIER_4
        IFaceTier.TIER_5 -> FaceTier.TIER_5
    }

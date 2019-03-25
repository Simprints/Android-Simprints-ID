package com.simprints.fingerprint.data.domain.matching

import com.simprints.moduleapi.fingerprint.requests.IMatchGroup

enum class MatchGroup {
    GLOBAL,
    USER,
    MODULE
}

fun IMatchGroup.toDomainClass(): MatchGroup =
    when(this) {
        IMatchGroup.GLOBAL -> MatchGroup.GLOBAL
        IMatchGroup.USER -> MatchGroup.USER
        IMatchGroup.MODULE -> MatchGroup.GLOBAL
    }

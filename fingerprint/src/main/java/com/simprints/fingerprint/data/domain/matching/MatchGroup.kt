package com.simprints.fingerprint.data.domain.matching

import com.simprints.moduleapi.fingerprint.requests.IMatchGroup
import  com.simprints.id.domain.GROUP as GROUP_CORE
import com.simprints.fingerprint.data.domain.matching.MatchGroup

enum class MatchGroup {
    GLOBAL,
    USER,
    MODULE
}

fun IMatchGroup.fromModuleApiToDomain(): MatchGroup =
    when(this) {
        IMatchGroup.GLOBAL -> MatchGroup.GLOBAL
        IMatchGroup.USER -> MatchGroup.USER
        IMatchGroup.MODULE -> MatchGroup.GLOBAL
    }

fun MatchGroup.fromDomainToCore() =
    when(this) {
        MatchGroup.GLOBAL -> GROUP_CORE.GLOBAL
        MatchGroup.USER -> GROUP_CORE.USER
        MatchGroup.MODULE -> GROUP_CORE.MODULE
    }

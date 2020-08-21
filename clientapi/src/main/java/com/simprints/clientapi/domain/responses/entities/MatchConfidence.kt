package com.simprints.clientapi.domain.responses.entities

import com.simprints.moduleapi.app.responses.IAppMatchConfidence

enum class MatchConfidence {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}

fun IAppMatchConfidence.fromModuleApiToDomain() = when(this) {
    IAppMatchConfidence.NONE -> MatchConfidence.NONE
    IAppMatchConfidence.LOW -> MatchConfidence.LOW
    IAppMatchConfidence.MEDIUM -> MatchConfidence.MEDIUM
    IAppMatchConfidence.HIGH -> MatchConfidence.HIGH
}

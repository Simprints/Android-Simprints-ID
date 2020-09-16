package com.simprints.clientapi.domain.responses

import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import com.simprints.clientapi.domain.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.app.responses.IAppIdentifyResponse


data class IdentifyResponse(val identifications: List<MatchResult>,
                            val sessionId: String) {

    constructor(response: IAppIdentifyResponse) : this(
        response.identifications.map {
            MatchResult(
                it.guid,
                it.confidenceScore,
                it.tier.fromModuleApiToDomain(),
                it.matchConfidence.fromModuleApiToDomain()
            )
        },
        response.sessionId
    )

}


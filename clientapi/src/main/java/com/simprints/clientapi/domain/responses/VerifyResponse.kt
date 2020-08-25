package com.simprints.clientapi.domain.responses
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import com.simprints.clientapi.domain.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.app.responses.IAppVerifyResponse


data class VerifyResponse(val matchResult: MatchResult) {

    constructor(response: IAppVerifyResponse):
        this(MatchResult(response.matchResult.guid,
            response.matchResult.confidenceScore,
            response.matchResult.tier.fromModuleApiToDomain(),
            response.matchResult.matchConfidence.fromModuleApiToDomain())
    )
}

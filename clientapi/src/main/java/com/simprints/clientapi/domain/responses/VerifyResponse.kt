package com.simprints.clientapi.domain.responses
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import com.simprints.moduleapi.app.responses.IAppVerifyResponse


data class VerifyResponse(val matchResult: MatchResult) {

    constructor(request: IAppVerifyResponse):
        this(MatchResult(request.matchResult.guid, request.matchResult.confidence, Tier.valueOf(request.matchResult.tier.name))
    )
}

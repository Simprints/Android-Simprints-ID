package com.simprints.feature.orchestrator.model.responses

import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponseTier
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppMatchResult(
    override val guid: String,
    override val confidenceScore: Int,
    override val tier: IAppResponseTier,
    override val matchConfidence: IAppMatchConfidence
) : IAppMatchResult

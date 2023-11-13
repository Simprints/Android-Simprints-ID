package com.simprints.feature.clientapi.mappers.response

import android.os.Parcelable
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppMatchResult
import com.simprints.core.domain.response.AppResponseTier
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class StubMatchResult(
    override val guid: String,
    override val confidenceScore: Int,
    override val tier: AppResponseTier,
    override val matchConfidence: AppMatchConfidence
) : IAppMatchResult, Parcelable

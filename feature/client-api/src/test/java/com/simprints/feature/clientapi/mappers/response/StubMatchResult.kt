package com.simprints.feature.clientapi.mappers.response

import android.os.Parcelable
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponseTier
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class StubMatchResult(
    override val guid: String,
    override val confidenceScore: Int,
    override val tier: IAppResponseTier,
    override val matchConfidence: IAppMatchConfidence
) : IAppMatchResult, Parcelable

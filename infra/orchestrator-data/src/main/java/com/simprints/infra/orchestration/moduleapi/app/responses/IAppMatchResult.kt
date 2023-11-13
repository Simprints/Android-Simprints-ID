package com.simprints.infra.orchestration.moduleapi.app.responses

import android.os.Parcelable
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.domain.response.AppResponseTier

interface IAppMatchResult : Parcelable {
    val guid: String
    val confidenceScore: Int
    val tier: AppResponseTier
    val matchConfidence: AppMatchConfidence
}

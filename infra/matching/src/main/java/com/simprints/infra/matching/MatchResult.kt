package com.simprints.infra.matching

import androidx.annotation.Keep
import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.sample.MatchComparisonResult
import com.simprints.core.domain.step.StepResult

@Keep
data class MatchResult(
    val results: List<MatchComparisonResult>,
    val sdk: ModalitySdkType,
) : StepResult

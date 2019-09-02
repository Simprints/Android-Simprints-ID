package com.simprints.id.domain.moduleapi.core

import com.simprints.id.orchestrator.steps.Step
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CoreStepResponse(val projectId: String): Step.Result

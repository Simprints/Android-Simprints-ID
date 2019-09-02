package com.simprints.id.domain.moduleapi.core

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CoreStepRequest(override val projectId: String,
                           override val userId: String,
                           override val moduleId: String,
                           override val metadata: String) : Step.Request, AppRequest {
    companion object {
        const val CORE_STEP_BUNDLE = "core_step_bundle"
    }
}

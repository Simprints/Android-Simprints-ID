package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.feature.consent.ConsentType
import com.simprints.id.orchestrator.steps.Step

interface CoreStepProcessor {

    fun buildStepConsent(consentType: ConsentType): Step

    fun buildFetchGuidStep(projectId: String, verifyGuid: String): Step

    fun buildConfirmIdentityStep(projectId: String, selectedGuid: String): Step

    fun buildAppEnrolLastBiometricsStep(
        projectId: String,
        userId: String,
        moduleId: String,
        previousSteps: List<Step>,
        sessionId: String?
    ): Step

    fun processResult(data: Intent?): Step.Result?
}

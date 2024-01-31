package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.consent.ConsentType
import com.simprints.id.orchestrator.steps.Step

interface CoreStepProcessor {

    fun buildStepSetup(): Step

    fun buildStepConsent(consentType: ConsentType): Step

    fun buildFetchGuidStep(projectId: String, verifyGuid: String): Step

    fun buildConfirmIdentityStep(projectId: String, selectedGuid: String): Step

    fun buildAppEnrolLastBiometricsStep(
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        previousSteps: List<Step>,
        sessionId: String?
    ): Step

    fun processResult(data: Intent?): Step.Result?
}
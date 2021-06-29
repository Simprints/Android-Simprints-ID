package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.core.domain.modality.Modality
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.core.requests.SetupPermission

interface CoreStepProcessor {

    fun buildStepSetup(modalities: List<Modality>, permissions: List<SetupPermission>): Step

    fun buildStepConsent(consentType: ConsentType): Step

    fun buildFetchGuidStep(projectId: String, verifyGuid: String): Step

    fun buildConfirmIdentityStep(projectId: String, sessionId: String, selectedGuid: String): Step

    fun buildAppEnrolLastBiometricsStep(projectId: String,
                                        userId: String,
                                        moduleId: String,
                                        previousSteps: List<Step>,
                                        sessionId: String?): Step

    fun processResult(data: Intent?): Step.Result?
}

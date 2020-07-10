package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.SetupPermission
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.requests.ConsentType

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

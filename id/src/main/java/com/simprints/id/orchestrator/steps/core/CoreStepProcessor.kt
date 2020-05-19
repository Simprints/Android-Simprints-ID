package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.requests.ConsentType

interface CoreStepProcessor {

    fun buildStepConsent(consentType: ConsentType): Step

    fun buildFetchGuidStep(projectId: String, verifyGuid: String): Step

    fun buildConfirmIdentityStep(projectId: String, sessionId: String, selectedGuid: String): Step

    fun buildAppEnrolLastBiometricsStep(projectId: String,
                                        userId: String,
                                        moduleId: String,
                                        fingerprintCaptureResponse: FingerprintCaptureResponse?,
                                        faceCaptureResponse: FaceCaptureResponse?,
                                        sessionId: String?): Step

    fun processResult(data: Intent?): Step.Result?
}

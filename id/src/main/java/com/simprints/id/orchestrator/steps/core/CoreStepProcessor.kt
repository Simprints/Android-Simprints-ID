package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.Step

interface CoreStepProcessor {

    fun buildStepConsent(consentType: ConsentType): Step

    fun buildStepVerify(projectId: String, verifyGuid: String): Step

    fun processResult(data: Intent?): Step.Result?
}

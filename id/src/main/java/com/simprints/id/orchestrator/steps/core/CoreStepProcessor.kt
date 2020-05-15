package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.requests.SetupPermission
import com.simprints.id.orchestrator.steps.Step

interface CoreStepProcessor {

    fun buildStepSetup(permissions: List<SetupPermission>): Step

    fun buildStepConsent(consentType: ConsentType): Step

    fun buildStepVerify(projectId: String, verifyGuid: String): Step

    fun processResult(data: Intent?): Step.Result?
}

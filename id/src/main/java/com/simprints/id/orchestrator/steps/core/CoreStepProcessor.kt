package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.Step

interface CoreStepProcessor {

    fun buildStepConsent(consentType: ConsentType): Step

    fun buildStepVerify(): Step

    fun processResult(resultCode: Int, data: Intent?): Step.Result?
}

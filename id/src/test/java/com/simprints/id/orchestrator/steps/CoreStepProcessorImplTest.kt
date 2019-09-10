package com.simprints.id.orchestrator.steps

import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import org.junit.Test

class CoreStepProcessorImplTest: BaseStepProcessorTest() {

    @Test
    fun stepProcessorShouldBuildRightStepForEnrol() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.ENROL)

        verifyCoreIntent<AskConsentRequest>(step, CoreRequestCode.CONSENT.value)
    }

    @Test
    fun stepProcessorShouldBuildRightStepForIdentify() {
        val step = CoreStepProcessorImpl().buildStepConsent(ConsentType.IDENTIFY)

        verifyCoreIntent<AskConsentRequest>(step, CoreRequestCode.CONSENT.value)
    }

    //TODO: Add verify test once implemented
}

package com.simprints.id.orchestrator.steps

import com.simprints.id.domain.moduleapi.core.CoreStepRequest
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import org.junit.Test

class CoreStepProcessorImplTest: BaseStepProcessorTest() {

    @Test
    fun stepProcessorShouldBuildRightStepForEnrol() {
        with(enrolAppRequest) {
            val step = CoreStepProcessorImpl().buildStepEnrolOrIdentify(projectId, userId, moduleId, metadata)

            verifyCoreIntent<CoreStepRequest>(step, CoreRequestCode.CONSENT.value)
        }
    }

    @Test
    fun stepProcessorShouldBuildRightStepForIdentify() {
        with(identifyAppRequest) {
            val step = CoreStepProcessorImpl().buildStepEnrolOrIdentify(projectId, userId, moduleId, metadata)

            verifyCoreIntent<CoreStepRequest>(step, CoreRequestCode.CONSENT.value)
        }
    }

    //TODO: Add verify test once implemented
}

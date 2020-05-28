package com.simprints.id.orchestrator.modality

import android.app.Activity
import com.google.common.truth.Truth
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_METADATA
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.moduleApi.AppEnrolLastBiometricsRequestApi
import com.simprints.id.domain.moduleapi.app.fromModuleApiToDomain
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ModalityFlowEnrolLastBiometricsTest {

    private lateinit var modalityFlowEnrolLastBiometrics: ModalityFlowEnrolLastBiometrics
    @MockK lateinit var coreProcessorMock: CoreStepProcessor
    @MockK lateinit var hotCache: HotCache
    private val appRequest = AppEnrolLastBiometricsRequestApi(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, GUID1)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        modalityFlowEnrolLastBiometrics = ModalityFlowEnrolLastBiometrics(coreProcessorMock, hotCache)
    }

    @Test
    fun startFlow_shouldBuildTheRightListOfSteps() {
        val previousSteps = listOf<Step>()
        every { hotCache.load() } returns previousSteps
        modalityFlowEnrolLastBiometrics.startFlow(appRequest.fromModuleApiToDomain(), emptyList())

        Truth.assertThat(modalityFlowEnrolLastBiometrics.steps).hasSize(1)
        verify { coreProcessorMock.buildAppEnrolLastBiometricsStep(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, previousSteps, GUID1) }
    }

    @Test
    fun notStartedStep_getNextStepToLaunch_returnTheRightStep() {
        val step = mockk<Step>()
        every { step.getStatus() } returns Step.Status.NOT_STARTED
        every { coreProcessorMock.buildAppEnrolLastBiometricsStep(any(), any(), any(), any(), any()) } returns step

        modalityFlowEnrolLastBiometrics.startFlow(appRequest.fromModuleApiToDomain(), emptyList())

        val next = modalityFlowEnrolLastBiometrics.getNextStepToLaunch()

        Truth.assertThat(next).isEqualTo(step)
    }

    @Test
    fun givenAGuidSelectActivityResult_handleIt_shouldReturnTheRightResult() {
        runBlocking {
            val step = mockk<Step>()
            every { step.requestCode } returns CoreRequestCode.LAST_BIOMETRICS_CORE.value
            modalityFlowEnrolLastBiometrics.steps.addAll(listOf(step))

            modalityFlowEnrolLastBiometrics.handleIntentResult(
                appRequest.fromModuleApiToDomain(),
                CoreRequestCode.LAST_BIOMETRICS_CORE.value,
                Activity.RESULT_OK,
                null)

            Truth.assertThat(step).isNotNull()
            verify { coreProcessorMock.processResult(any()) }
        }
    }
}

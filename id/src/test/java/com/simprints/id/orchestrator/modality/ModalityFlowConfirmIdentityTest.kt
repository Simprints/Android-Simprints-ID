package com.simprints.id.orchestrator.modality

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.moduleApi.AppConfirmaConfirmIdentityRequestModuleApi
import com.simprints.id.domain.moduleapi.app.fromModuleApiToDomain
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

class ModalityFlowConfirmIdentityTest {

    private lateinit var modalityFlowConfirmIdentity: ModalityFlowConfirmIdentity
    @MockK lateinit var coreProcessorMock: CoreStepProcessor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        modalityFlowConfirmIdentity = ModalityFlowConfirmIdentity(coreProcessorMock)
    }

    @Test
    fun startFlow_shouldBuildTheRightListOfSteps() {
        val appRequest = AppConfirmaConfirmIdentityRequestModuleApi(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, GUID1, GUID2)
        modalityFlowConfirmIdentity.startFlow(appRequest.fromModuleApiToDomain(), emptyList())

        assertThat(modalityFlowConfirmIdentity.steps).hasSize(1)
        verify { coreProcessorMock.buildConfirmIdentityStep(DEFAULT_PROJECT_ID, GUID1, GUID2) }
    }

    @Test
    fun notStartedStep_getNextStepToLaunch_returnTheRightStep() {
        val appRequest = AppConfirmaConfirmIdentityRequestModuleApi(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, GUID1, GUID2)
        val step = mockk<Step>()
        every { step.getStatus() } returns Step.Status.NOT_STARTED
        every { coreProcessorMock.buildConfirmIdentityStep(any(), any(), any()) } returns step

        modalityFlowConfirmIdentity.startFlow(appRequest.fromModuleApiToDomain(), emptyList())

        val next = modalityFlowConfirmIdentity.getNextStepToLaunch()

        assertThat(next).isNotNull()
    }

    @Test
    fun givenAGuidSelectActivityResult_handleIt_shouldReturnTheRightResult() {
        runBlocking {
            val appRequest = AppConfirmaConfirmIdentityRequestModuleApi(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, GUID1, GUID2)
            val step = mockk<Step>()
            every { step.requestCode } returns CoreRequestCode.GUID_SELECTION_CODE.value
            modalityFlowConfirmIdentity.steps.addAll(listOf(step))

            modalityFlowConfirmIdentity.handleIntentResult(
                appRequest.fromModuleApiToDomain(),
                CoreRequestCode.GUID_SELECTION_CODE.value,
                Activity.RESULT_OK,
                null)

            assertThat(step).isNotNull()
            verify { coreProcessorMock.processResult(any()) }
        }
    }
}

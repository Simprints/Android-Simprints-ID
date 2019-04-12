package com.simprints.id.orchestrator

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.*
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.exceptions.unexpected.UnexpectedErrorInModalFlow
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.orchestrator.modality.flows.FaceModalityFlow
import com.simprints.id.orchestrator.modality.flows.FingerprintModalityFlow
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test


class OrchestratorManagerImplTest {

    private lateinit var captorArgsForBuilderModalityFlow: KArgumentCaptor<List<ModalityFlow>>
    private lateinit var modalityFlowBuilderMock: ModalityFlowFactory

    private var firstModalityStepRequest = ModalityStepRequest(1, Intent())
    private var firstModalityResponse = mock<ModalityResponse>()

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()

        captorArgsForBuilderModalityFlow = argumentCaptor()
        modalityFlowBuilderMock = mock()
    }

    @Test
    fun givenFaceModality_buildAFlowModality_itShouldContainAFaceModality() {
        val orchestrator = buildOrchestratorToBuildFlowModality(FACE)

        orchestrator.flowModality

        verifyBuilderCalledWithRightModalFlows(modalityFlowBuilderMock, captorArgsForBuilderModalityFlow,
            listOf(FaceModalityFlow::class.java))
    }

    @Test
    fun startFlowModality_orchestratorShouldStartTheFlowModality() {
        val orchestrator = buildOrchestratorToStartFlow(listOf(firstModalityStepRequest))

        startFlowModal(orchestrator).assertResult(firstModalityStepRequest)
    }

    @Test
    fun startFlowModality_orchestratorShouldClearPreviousResults() {
        val orchestrator = buildOrchestratorToStartFlow(listOf(firstModalityStepRequest))
        orchestrator.stepsResults.add(firstModalityResponse)

        startFlowModal(orchestrator).assertValueCount(1)

        assertThat(orchestrator.stepsResults.size).isEqualTo(0)
    }

    @Test
    fun flowModalStarted_modalFlowReturnsAResponse_orchestratorShouldHandleIt() {
        val orchestrator =
            spy(buildOrchestratorToStartFlow(listOf(firstModalityStepRequest), listOf(firstModalityResponse))
                .apply { appRequest = mock() })

        startFlowModal(orchestrator).assertValueCount(1)

        verifyOnce(orchestrator) { subscribeForStepsIntentResults(anyNotNull(), anyNotNull()) }
        verifyOnce(orchestrator) { addNewStepIntentResult(anyNotNull()) }
    }

    @Test
    fun flowModalStarted_modalFlowReturnsAnError_orchestratorShouldHandleIt() {
        val orchestrator =
            spy(buildOrchestratorToStartFlow(listOf(firstModalityStepRequest))
                .apply { appRequest = mock() })

        startFlowModal(orchestrator)

        verifyOnce(orchestrator) { subscribeForStepsIntentResults(anyNotNull(), anyNotNull()) }
        verifyOnce(orchestrator) { buildAndEmitFinalResult(anyNotNull(), anyNotNull()) }
    }

    @Test
    fun flowModalStarted_modalFlowEmitsAnError_orchestratorShouldHandleIt() {
        val modalFlowMock = mock<ModalityFlow>()
        whenever { modalityFlowBuilderMock.buildModalityFlow(any()) } thenReturn modalFlowMock
        whenever { modalFlowMock.nextModalityStepRequest } thenReturn Observable.empty()
        whenever { modalFlowMock.modalityResponses } thenReturn Observable.error(UnexpectedErrorInModalFlow())
        val orchestrator = spy(OrchestratorManagerImpl(FACE, modalityFlowBuilderMock, mock(), mock())
            .apply { appRequest = mock() })

        startFlowModal(orchestrator)

        verifyOnce(orchestrator) { emitErrorAsFinalResult() }
    }

    @Test
    fun flowModalStarted_someActivityReturnsAnIntent_orchestratorShouldNotifyTheModalFlow() {
        val orchestrator =
            spy(buildOrchestratorToStartFlow(listOf(firstModalityStepRequest))
                .apply { appRequest = mock() })

        startFlowModal(orchestrator)

        val intentReceived = Intent()
        orchestrator.onModalStepRequestDone(0, 0, intentReceived)
        verifyOnce(orchestrator.flowModality) { handleIntentResponse(0, 0, intentReceived) }
    }

    private fun startFlowModal(orchestrator: OrchestratorManagerImpl): TestObserver<ModalityStepRequest> =
        orchestrator
            .startFlow(mock(), "")
            .test()

    @Test
    fun givenFaceModality_buildAFlowModality_shouldContainAFingerprintModality() {
        val orchestrator = buildOrchestratorToBuildFlowModality(FACE)

        orchestrator.flowModality

        verifyBuilderCalledWithRightModalFlows(modalityFlowBuilderMock, captorArgsForBuilderModalityFlow,
            listOf(FaceModalityFlow::class.java))
    }


    @Test
    fun givenFingerprintModality_buildAFlowModality_shouldContainAFingerprintModality() {
        val orchestrator = buildOrchestratorToBuildFlowModality(FINGER)

        orchestrator.flowModality

        verifyBuilderCalledWithRightModalFlows(modalityFlowBuilderMock, captorArgsForBuilderModalityFlow,
            listOf(FingerprintModalityFlow::class.java))
    }

    @Test
    fun givenFaceFingerprintModality_buildAFlowModality_shouldContainAFaceAndFingerprintModals() {
        val orchestrator = buildOrchestratorToBuildFlowModality(FACE_FINGER)

        orchestrator.flowModality

        verifyBuilderCalledWithRightModalFlows(modalityFlowBuilderMock, captorArgsForBuilderModalityFlow,
            listOf(FaceModalityFlow::class.java, FingerprintModalityFlow::class.java))
    }

    @Test
    fun givenFingerprintFaceModality_buildAFlowModality_shouldContainAFingerprintAndFaceModals() {
        val orchestrator = buildOrchestratorToBuildFlowModality(FINGER_FACE)

        orchestrator.flowModality

        verifyBuilderCalledWithRightModalFlows(modalityFlowBuilderMock, captorArgsForBuilderModalityFlow,
            listOf(FingerprintModalityFlow::class.java, FaceModalityFlow::class.java))
    }

    private fun buildOrchestratorToStartFlow(mockModalityRequests: List<ModalityStepRequest> = emptyList(),
                                             mockModalityResponses: List<ModalityResponse> = emptyList()): OrchestratorManagerImpl {

        val modalFlowMock = mock<ModalityFlow>()
        whenever { modalityFlowBuilderMock.buildModalityFlow(any()) } thenReturn modalFlowMock
        whenever { modalFlowMock.nextModalityStepRequest } thenReturn Observable.fromIterable(mockModalityRequests)
        whenever { modalFlowMock.modalityResponses } thenReturn Observable.fromIterable(mockModalityResponses)
        return OrchestratorManagerImpl(FACE, modalityFlowBuilderMock, mock(), mock())
    }

    private fun buildOrchestratorToBuildFlowModality(modality: Modality) =
        OrchestratorManagerImpl(modality, modalityFlowBuilderMock, mock(), mock())
            .apply { appRequest = mock() }

    // Verifies the modalFlow builder is called from the
    // orchestrator with the right Modality Flow params
    private fun verifyBuilderCalledWithRightModalFlows(modalityFlowBuilderMock: ModalityFlowFactory,
                                                       captorArgsForBuilderModalityFlow: KArgumentCaptor<List<ModalityFlow>>,
                                                       modalFlowClasses: List<Class<*>>) {

        verify(modalityFlowBuilderMock, times(1)).buildModalityFlow(captorArgsForBuilderModalityFlow.capture())
        val steps = captorArgsForBuilderModalityFlow.firstValue
        steps.forEachIndexed { index, modalFlow ->
            assertThat(modalFlow).isInstanceOf(modalFlowClasses[index])
        }
        assertThat(steps.size).isEqualTo(modalFlowClasses.size)
    }
}

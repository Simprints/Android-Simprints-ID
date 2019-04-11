package com.simprints.id.orchestrator

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import com.simprints.id.domain.modal.Modal
import com.simprints.id.domain.modal.Modal.*
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.exceptions.unexpected.UnexpectedErrorInModalFlow
import com.simprints.id.orchestrator.modals.ModalFlowFactory
import com.simprints.id.orchestrator.modals.ModalStepRequest
import com.simprints.id.orchestrator.modals.flows.FaceModalFlow
import com.simprints.id.orchestrator.modals.flows.FingerprintModalFlow
import com.simprints.id.orchestrator.modals.flows.interfaces.ModalFlow
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

    private lateinit var captorArgsForBuilderModalFlow: KArgumentCaptor<List<ModalFlow>>
    private lateinit var modalFlowBuilderMock: ModalFlowFactory

    private var firstModalStepRequest = ModalStepRequest(1, Intent())
    private var firstModalResponse = mock<ModalResponse>()

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()

        captorArgsForBuilderModalFlow = argumentCaptor()
        modalFlowBuilderMock = mock()
    }

    @Test
    fun givenFaceModal_buildAFlowModal_thenItShouldContainAFaceModal() {
        val orchestrator = buildOrchestratorToBuildFlowModal(FACE)

        orchestrator.flowModal

        verifyBuilderCalledWithRightModalFlows(modalFlowBuilderMock, captorArgsForBuilderModalFlow,
            listOf(FaceModalFlow::class.java))
    }

    @Test
    fun startFlowModal_thenOrchestratorShouldStartTheFlowModal() {
        val orchestrator = buildOrchestratorToStartFlow(listOf(firstModalStepRequest))

        startFlowModal(orchestrator).assertResult(firstModalStepRequest)
    }

    @Test
    fun startFlowModal_thenOrchestratorShouldClearPreviousResults() {
        val orchestrator = buildOrchestratorToStartFlow(listOf(firstModalStepRequest))
        orchestrator.stepsResults.add(firstModalResponse)

        startFlowModal(orchestrator).assertValueCount(1)

        assertThat(orchestrator.stepsResults.size).isEqualTo(0)
    }

    @Test
    fun flowModalStarted_modalFlowReturnsAResponse_thenOrchestratorShouldHandleIt() {
        val orchestrator =
            spy(buildOrchestratorToStartFlow(listOf(firstModalStepRequest), listOf(firstModalResponse))
                .apply { appRequest = mock() })

        startFlowModal(orchestrator).assertValueCount(1)

        verifyOnce(orchestrator) { subscribeForStepsIntentResults(anyNotNull(), anyNotNull()) }
        verifyOnce(orchestrator) { addNewStepIntentResult(anyNotNull()) }
    }

    @Test
    fun flowModalStarted_modalFlowReturnsAnError_thenOrchestratorShouldHandleIt() {
        val orchestrator =
            spy(buildOrchestratorToStartFlow(listOf(firstModalStepRequest))
                .apply { appRequest = mock() })

        startFlowModal(orchestrator)

        verifyOnce(orchestrator) { subscribeForStepsIntentResults(anyNotNull(), anyNotNull()) }
        verifyOnce(orchestrator) { buildAndEmitFinalResult(anyNotNull(), anyNotNull()) }
    }

    @Test
    fun flowModalStarted_modalFlowEmitsAnError_orchestratorShouldHandleIt() {
        val modalFlowMock = mock<ModalFlow>()
        whenever { modalFlowBuilderMock.buildModalFlow(any()) } thenReturn modalFlowMock
        whenever { modalFlowMock.nextModalStepRequest } thenReturn Observable.empty()
        whenever { modalFlowMock.modalResponses } thenReturn Observable.error(UnexpectedErrorInModalFlow())
        val orchestrator = spy(OrchestratorManagerImpl(FACE, modalFlowBuilderMock, mock(), mock())
            .apply { appRequest = mock() })

        startFlowModal(orchestrator)

        verifyOnce(orchestrator) { emitErrorAsFinalResult() }
    }

    @Test
    fun flowModalStarted_someActivityReturnsAnIntent_orchestratorShouldNotifyTheModalFlow() {
        val orchestrator =
            spy(buildOrchestratorToStartFlow(listOf(firstModalStepRequest))
                .apply { appRequest = mock() })

        startFlowModal(orchestrator)

        val intentReceived = Intent()
        orchestrator.onModalStepRequestDone(0, 0, intentReceived)
        verifyOnce(orchestrator.flowModal) { handleIntentResponse(0, 0, intentReceived) }
    }

    private fun startFlowModal(orchestrator: OrchestratorManagerImpl): TestObserver<ModalStepRequest> =
        orchestrator
            .startFlow(mock(), "")
            .test()

    @Test
    fun givenFaceModal_buildAFlowModal_itShouldContainAFingerprintModal() {
        val orchestrator = buildOrchestratorToBuildFlowModal(FACE)

        orchestrator.flowModal

        verifyBuilderCalledWithRightModalFlows(modalFlowBuilderMock, captorArgsForBuilderModalFlow,
            listOf(FaceModalFlow::class.java))
    }


    @Test
    fun givenFingerprintModal_buildAFlowModal_itShouldContainAFingerprintModal() {
        val orchestrator = buildOrchestratorToBuildFlowModal(FINGER)

        orchestrator.flowModal

        verifyBuilderCalledWithRightModalFlows(modalFlowBuilderMock, captorArgsForBuilderModalFlow,
            listOf(FingerprintModalFlow::class.java))
    }

    @Test
    fun givenFaceFingerprintModal_buildAFlowModal_itShouldContainAFaceAndFingerprintModals() {
        val orchestrator = buildOrchestratorToBuildFlowModal(FACE_FINGER)

        orchestrator.flowModal

        verifyBuilderCalledWithRightModalFlows(modalFlowBuilderMock, captorArgsForBuilderModalFlow,
            listOf(FaceModalFlow::class.java, FingerprintModalFlow::class.java))
    }

    @Test
    fun givenFingerprintFaceModal_buildAFlowModal_itShouldContainAFingerprintAndFaceModals() {
        val orchestrator = buildOrchestratorToBuildFlowModal(FINGER_FACE)

        orchestrator.flowModal

        verifyBuilderCalledWithRightModalFlows(modalFlowBuilderMock, captorArgsForBuilderModalFlow,
            listOf(FingerprintModalFlow::class.java, FaceModalFlow::class.java))
    }

    private fun buildOrchestratorToStartFlow(mockModalRequests: List<ModalStepRequest> = emptyList(),
                                             mockModalResponses: List<ModalResponse> = emptyList()): OrchestratorManagerImpl {

        val modalFlowMock = mock<ModalFlow>()
        whenever { modalFlowBuilderMock.buildModalFlow(any()) } thenReturn modalFlowMock
        whenever { modalFlowMock.nextModalStepRequest } thenReturn Observable.fromIterable(mockModalRequests)
        whenever { modalFlowMock.modalResponses } thenReturn Observable.fromIterable(mockModalResponses)
        return OrchestratorManagerImpl(FACE, modalFlowBuilderMock, mock(), mock())
    }

    private fun buildOrchestratorToBuildFlowModal(modal: Modal) =
        OrchestratorManagerImpl(modal, modalFlowBuilderMock, mock(), mock())
            .apply { appRequest = mock() }

    // Verifies the modalFlow builder is called from the
    // orchestrator with the right Modal Flow params
    private fun verifyBuilderCalledWithRightModalFlows(modalFlowBuilderMock: ModalFlowFactory,
                                                       captorArgsForBuilderModalFlow: KArgumentCaptor<List<ModalFlow>>,
                                                       modalFlowClasses: List<Class<*>>) {

        verify(modalFlowBuilderMock, times(1)).buildModalFlow(captorArgsForBuilderModalFlow.capture())
        val steps = captorArgsForBuilderModalFlow.firstValue
        steps.forEachIndexed { index, modalFlow ->
            assertThat(modalFlow).isInstanceOf(modalFlowClasses[index])
        }
        assertThat(steps.size).isEqualTo(modalFlowClasses.size)
    }
}

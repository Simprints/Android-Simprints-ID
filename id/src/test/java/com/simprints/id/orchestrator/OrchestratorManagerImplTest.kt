package com.simprints.id.orchestrator

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.spy
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.exceptions.unexpected.UnexpectedErrorInModalFlow
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.ModalityStepRequest
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
    private lateinit var modalitiesFlowFactoryMock: ModalityFlowFactory
    private lateinit var firstModalityResponseMock: ModalityResponse

    private var firstModalityStepRequest = ModalityStepRequest(1, Intent())

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()

        captorArgsForBuilderModalityFlow = argumentCaptor()
        modalitiesFlowFactoryMock = mock()
        firstModalityResponseMock = mock()
    }

    @Test
    fun startFlowModality_orchestratorShouldStartTheFlowModality() {
        val orchestrator = buildOrchestratorToStartFlow(listOf(firstModalityStepRequest))

        startFlowModal(orchestrator).assertResult(firstModalityStepRequest)
    }

    @Test
    fun startFlowModality_orchestratorShouldClearPreviousResults() {
        val orchestrator = buildOrchestratorToStartFlow(listOf(firstModalityStepRequest))
        orchestrator.stepsResults.add(firstModalityResponseMock)

        startFlowModal(orchestrator).assertValueCount(1)

        assertThat(orchestrator.stepsResults.size).isEqualTo(0)
    }

    @Test
    fun flowModalStarted_modalFlowReturnsAResponse_orchestratorShouldHandleIt() {
        val orchestrator =
            spy(buildOrchestratorToStartFlow(listOf(firstModalityStepRequest), listOf(firstModalityResponseMock))
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
        whenever { modalitiesFlowFactoryMock.buildModalityFlow(any(), any()) } thenReturn modalFlowMock
        whenever { modalFlowMock.modalityStepRequests } thenReturn Observable.empty()
        whenever { modalFlowMock.modalityResponses } thenReturn Observable.error(UnexpectedErrorInModalFlow())
        val orchestrator = spy(OrchestratorManagerImpl(FACE, modalitiesFlowFactoryMock, mock())
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
        verifyOnce(orchestrator.modalitiesFlow) { handleIntentResponse(0, 0, intentReceived) }
    }

    private fun startFlowModal(orchestrator: OrchestratorManagerImpl): TestObserver<ModalityStepRequest> =
        orchestrator
            .startFlow(mock(), "")
            .test()

    private fun buildOrchestratorToStartFlow(mockModalityRequests: List<ModalityStepRequest> = emptyList(),
                                             mockModalityResponses: List<ModalityResponse> = emptyList()): OrchestratorManagerImpl {

        val modalFlowMock = mock<ModalityFlow>()
        whenever { modalitiesFlowFactoryMock.buildModalityFlow(any(), any()) } thenReturn modalFlowMock
        whenever { modalFlowMock.modalityStepRequests } thenReturn Observable.fromIterable(mockModalityRequests)
        whenever { modalFlowMock.modalityResponses } thenReturn Observable.fromIterable(mockModalityResponses)
        return OrchestratorManagerImpl(FACE, modalitiesFlowFactoryMock, mock())
    }
}

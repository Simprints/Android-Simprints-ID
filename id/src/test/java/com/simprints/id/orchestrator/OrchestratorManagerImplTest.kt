package com.simprints.id.orchestrator

import android.content.Intent
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.exceptions.unexpected.UnexpectedErrorInModalFlow
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.*
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
    fun flowModalStarted_modalFlowReturnsAResponse_orchestratorShouldHandleIt() {
        val orchestrator =
            spy(buildOrchestratorToStartFlow(listOf(firstModalityStepRequest), listOf(firstModalityResponseMock))
                .apply { appRequest = mock() })

        startFlowModal(orchestrator)
            .assertValueCount(1)
    }

    @Test
    fun flowModalStarted_modalFlowEmitsAnError_orchestratorShouldHandleIt() {
        val modalFlowMock = mock<ModalityFlow>()
        whenever { modalitiesFlowFactoryMock.buildModalityFlow(anyNotNull(), anyNotNull()) } thenReturn modalFlowMock
        whenever { modalFlowMock.modalityResponses } thenReturn Observable.error(UnexpectedErrorInModalFlow())
        val orchestrator = spy(OrchestratorManagerImpl(FACE, modalitiesFlowFactoryMock, mock())
            .apply { appRequest = mock() })

        orchestrator.getAppResponse()
            .test()
            .assertError(UnexpectedErrorInModalFlow::class.java)
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
        whenever { modalitiesFlowFactoryMock.buildModalityFlow(anyNotNull(), anyNotNull()) } thenReturn modalFlowMock
        whenever { modalFlowMock.modalityStepRequests } thenReturn Observable.fromIterable(mockModalityRequests)
        whenever { modalFlowMock.modalityResponses } thenReturn Observable.fromIterable(mockModalityResponses)
        return OrchestratorManagerImpl(FACE, modalitiesFlowFactoryMock, mock())
    }
}

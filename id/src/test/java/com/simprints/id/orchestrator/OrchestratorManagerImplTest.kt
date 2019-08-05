package com.simprints.id.orchestrator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.flows.FaceModalityFlow
import com.simprints.id.orchestrator.modality.flows.MultiModalitiesFlowBase
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class OrchestratorManagerImplTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var modalityFlowFactoryMock: ModalityFlowFactory

    private val appRequest = AppEnrolRequest("some_project_id", "some_user_id", "some_module_id", "some_metadata")

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .coroutinesMainThread()
            .rescheduleRxMainThread()

        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun startFlowModality_orchestratorShouldStartTheFlowModality() {
        val multiModalitiesFlow = MultiModalitiesFlowBase(listOf(FaceModalityFlow(appRequest, "com.simprints.id")))
        whenever { modalityFlowFactoryMock.buildModalityFlow(anyNotNull(), anyNotNull()) } thenReturn multiModalitiesFlow

        val orchestrator = OrchestratorManagerImpl(FACE, modalityFlowFactoryMock, mock())
        runBlocking {
            orchestrator.initOrchestrator(appRequest, "")
        }

        Truth.assertThat(orchestrator.nextIntent.value?.requestCode).isEqualTo(FaceModalityFlow.REQUEST_CODE_FACE)
    }

//    @Test
//    fun flowModalStarted_modalFlowReturnsAResponse_orchestratorShouldHandleIt() {
//        val orchestrator =
//            spy(buildOrchestratorToStartFlow(listOf(firstModalityStepRequest), listOf(firstModalityResponseMock))
//                .apply { appRequest = mock() })
//
//        startFlowModal(orchestrator)
//            .assertValueCount(1)
//    }
//
//    @Test
//    fun flowModalStarted_modalFlowEmitsAnError_orchestratorShouldHandleIt() {
//        val modalFlowMock = mock<ModalityFlow>()
//        whenever { modalityFlowFactoryMock.buildModalityFlow(anyNotNull(), anyNotNull()) } thenReturn modalFlowMock
//        whenever { modalFlowMock.modalityResponses } thenReturn Observable.error(UnexpectedErrorInModalFlow())
//        val orchestrator = spy(OrchestratorManagerImpl(FACE, modalityFlowFactoryMock, mock())
//            .apply { appRequest = mock() })
//
//        orchestrator.getAppResponse()
//            .test()
//            .assertError(UnexpectedErrorInModalFlow::class.java)
//    }
//
//    @Test
//    fun flowModalStarted_someActivityReturnsAnIntent_orchestratorShouldNotifyTheModalFlow() {
//        val orchestrator =
//            spy(buildOrchestratorToStartFlow(listOf(firstModalityStepRequest))
//                .apply { appRequest = mock() })
//
//        startFlowModal(orchestrator)
//
//        val intentReceived = Intent()
//        orchestrator.onModalStepRequestDone(0, 0, intentReceived)
//        verifyOnce(orchestrator.modalitiesFlow) { handleIntentResult(0, 0, intentReceived) }
//    }

//    private fun startFlowModal(orchestrator: OrchestratorManagerImpl): TestObserver<ModalityStepRequest> =
//        orchestrator.nextIntent
//            .startFlow(mock(), "")
//            .test()
//
//    private fun buildOrchestratorToStartFlow(): OrchestratorManagerImpl {
//
//        val modalFlowMock = mock<ModalityFlow>()
//        whenever { modalityFlowFactoryMock.buildModalityFlow(anyNotNull(), anyNotNull()) } thenReturn modalFlowMock
//        return OrchestratorManagerImpl(FACE, modalityFlowFactoryMock, mock())
//    }
}

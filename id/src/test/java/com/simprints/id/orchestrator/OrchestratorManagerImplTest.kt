package com.simprints.id.orchestrator

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.simprints.id.domain.modal.Modal
import com.simprints.id.orchestrator.modals.ModalFlowBuilder
import com.simprints.id.orchestrator.modals.flows.FaceModalFlow
import com.simprints.id.orchestrator.modals.flows.FingerprintModalFlow
import com.simprints.id.orchestrator.modals.flows.interfaces.ModalFlow
import com.simprints.testtools.common.syntax.mock
import org.junit.Test


class OrchestratorManagerImplTest {

    @Test
    fun givenFaceModal_thenOrchestratorShouldUseAFaceFlow() {
        val captorForListOfModalFlows = argumentCaptor<List<ModalFlow>>()
        val modalFlowBuilderMock = mock<ModalFlowBuilder>()
        val orchestrator = OrchestratorManagerImpl(Modal.FACE, modalFlowBuilderMock, mock())
        orchestrator.appRequest = mock()

        orchestrator.flowModal

        verify(modalFlowBuilderMock, times(1)).buildModalFlow(captorForListOfModalFlows.capture())
        val steps = captorForListOfModalFlows.firstValue
        assertThat(steps.first()).isInstanceOf(FaceModalFlow::class.java)
        assertThat(steps.size).isEqualTo(1)
    }

    @Test
    fun givenFingerModal_thenOrchestratorShouldUseAFingerFlow() {
        val captorForListOfModalFlows = argumentCaptor<List<ModalFlow>>()
        val modalFlowBuilderMock = mock<ModalFlowBuilder>()
        val orchestrator = OrchestratorManagerImpl(Modal.FINGER, modalFlowBuilderMock, mock())
        orchestrator.appRequest = mock()

        orchestrator.flowModal

        verify(modalFlowBuilderMock, times(1)).buildModalFlow(captorForListOfModalFlows.capture())
        val steps = captorForListOfModalFlows.firstValue
        assertThat(steps.first()).isInstanceOf(FingerprintModalFlow::class.java)
        assertThat(steps.size).isEqualTo(1)
    }

    @Test
    fun givenFaceFingerModal_thenOrchestratorShouldUseFaceFingerFlows() {
        val captorForListOfModalFlows = argumentCaptor<List<ModalFlow>>()
        val modalFlowBuilderMock = mock<ModalFlowBuilder>()
        val orchestrator = OrchestratorManagerImpl(Modal.FACE_FINGER, modalFlowBuilderMock, mock())
        orchestrator.appRequest = mock()

        orchestrator.flowModal

        verify(modalFlowBuilderMock, times(1)).buildModalFlow(captorForListOfModalFlows.capture())
        val steps = captorForListOfModalFlows.firstValue
        assertThat(steps.first()).isInstanceOf(FaceModalFlow::class.java)
        assertThat(steps[1]).isInstanceOf(FingerprintModalFlow::class.java)
        assertThat(steps.size).isEqualTo(2)
    }

    @Test
    fun givenFingerFaceModal_thenOrchestratorShouldUseFingerFaceFlows() {
        val captorForListOfModalFlows = argumentCaptor<List<ModalFlow>>()
        val modalFlowBuilderMock = mock<ModalFlowBuilder>()
        val orchestrator = OrchestratorManagerImpl(Modal.FINGER_FACE, modalFlowBuilderMock, mock())
        orchestrator.appRequest = mock()

        orchestrator.flowModal

        verify(modalFlowBuilderMock, times(1)).buildModalFlow(captorForListOfModalFlows.capture())
        val steps = captorForListOfModalFlows.firstValue
        assertThat(steps.first()).isInstanceOf(FingerprintModalFlow::class.java)
        assertThat(steps[1]).isInstanceOf(FaceModalFlow::class.java)
        assertThat(steps.size).isEqualTo(2)
    }
}

package com.simprints.id.orchestrator

import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.orchestrator.modality.ModalityFlow
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ModalityFlowFactoryImplTest {

    private lateinit var modalityFlowFactory: ModalityFlowFactory
    @MockK lateinit var enrolFlow: ModalityFlow
    @MockK lateinit var identifyFlow: ModalityFlow
    @MockK lateinit var verifyFlow: ModalityFlow

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        modalityFlowFactory = ModalityFlowFactoryImpl(enrolFlow, verifyFlow, identifyFlow)
    }

    @Test
    fun enrolFlow_factoryShouldReturnTheRightModalityFlow() {
        modalityFlowFactory.createModalityFlow(enrolAppRequest, listOf(FACE))
        verifyModalityFlowStarted(enrolFlow)
    }

    @Test
    fun identifyFlow_factoryShouldReturnTheRightModalityFlow() {
        modalityFlowFactory.createModalityFlow(identifyAppRequest, listOf(FACE))
        verifyModalityFlowStarted(identifyFlow)
    }

    @Test
    fun verifyFlow_factoryShouldReturnTheRightModalityFlow() {
        modalityFlowFactory.createModalityFlow(verifyAppRequest, listOf(FACE))
        verifyModalityFlowStarted(verifyFlow)
    }

    private fun verifyModalityFlowStarted(modalityFlow: ModalityFlow) {
        verify(exactly = 1) { modalityFlow.startFlow(any(), any()) }
    }
}

package com.simprints.id.orchestrator

import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.verifyOnce
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ModalityFlowFactoryImplTest {

    private lateinit var modalityFlowFactory: ModalityFlowFactory
    @Mock lateinit var enrolFlow: ModalityFlow
    @Mock lateinit var identifyFlow: ModalityFlow
    @Mock lateinit var verifyFlow: ModalityFlow

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        modalityFlowFactory = ModalityFlowFactoryImpl(enrolFlow, verifyFlow, identifyFlow)
    }

    @Test
    fun enrolFlow_factoryShouldReturnTheRightModalityFlow() {
        modalityFlowFactory.startModalityFlow(enrolAppRequest, listOf(FACE))
        verifyModalityFlowStarted(enrolFlow)
    }

    @Test
    fun identifyFlow_factoryShouldReturnTheRightModalityFlow() {
        modalityFlowFactory.startModalityFlow(identifyAppRequest, listOf(FACE))
        verifyModalityFlowStarted(identifyFlow)
    }

    @Test
    fun verifyFlow_factoryShouldReturnTheRightModalityFlow() {
        modalityFlowFactory.startModalityFlow(verifyAppRequest, listOf(FACE))
        verifyModalityFlowStarted(verifyFlow)
    }

    private fun verifyModalityFlowStarted(modalityFlow: ModalityFlow) {
        verifyOnce(modalityFlow) { startFlow(anyNotNull(), anyNotNull()) }
    }
}

package com.simprints.id.orchestrator

import com.simprints.id.orchestrator.modality.ModalityFlow
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ModalityFlowFactoryImplTest {

    private lateinit var modalityFlowFactory: ModalityFlowFactory

    @MockK
    lateinit var enrolFlow: ModalityFlow

    @MockK
    lateinit var identifyFlow: ModalityFlow

    @MockK
    lateinit var verifyFlow: ModalityFlow

    @MockK
    lateinit var confirmationIdentityFlow: ModalityFlow

    @MockK
    lateinit var enrolLastBiometricsFlow: ModalityFlow

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        modalityFlowFactory = ModalityFlowFactoryImpl(
            enrolFlow,
            verifyFlow,
            identifyFlow,
            confirmationIdentityFlow,
            enrolLastBiometricsFlow
        )
    }

    @Test
    fun enrolFlow_factoryShouldReturnTheRightModalityFlow() = runTest {
        modalityFlowFactory.createModalityFlow(enrolAppRequest)
        verifyModalityFlowStarted(enrolFlow)
    }

    @Test
    fun identifyFlow_factoryShouldReturnTheRightModalityFlow() = runTest {
        modalityFlowFactory.createModalityFlow(identifyAppRequest)
        verifyModalityFlowStarted(identifyFlow)
    }

    @Test
    fun verifyFlow_factoryShouldReturnTheRightModalityFlow() = runTest {
        modalityFlowFactory.createModalityFlow(verifyAppRequest)
        verifyModalityFlowStarted(verifyFlow)
    }

    @Test
    fun enrolLastBiometricFlow_factoryShouldReturnTheRightModalityFlow() = runTest {
        modalityFlowFactory.createModalityFlow(enrolLastBiometricsRequest)
        verifyModalityFlowStarted(enrolLastBiometricsFlow)
    }

    @Test
    fun confirmationFlow_factoryShouldReturnTheRightModalityFlow() = runTest {
        modalityFlowFactory.createModalityFlow(confirmationRequest)
        verifyModalityFlowStarted(confirmationIdentityFlow)
    }

    private fun verifyModalityFlowStarted(modalityFlow: ModalityFlow) {
        coVerify(exactly = 1) { modalityFlow.startFlow(any()) }
    }
}

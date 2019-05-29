package com.simprints.id.orchestrator.modality.flows

import android.app.Activity
import android.content.Intent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt

class SingleModalityFlowBaseTest {

    companion object {
        const val REQUEST_CODE_FOR_SINGLE_MODALITY_FLOW = 1
    }

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()
    }

    @Test
    fun givenSingleModalityFlow_modalityStepRequestsGetsSubscribed_nextModalityStepRequestShouldBeEmitted() {
        val singleModalityFlowBase = SingleModalityFlowBaseImpl()

        val testObserver = singleModalityFlowBase.modalityStepRequests
            .test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
    }

    @Test
    fun givenSingleModalityFlow_receivesASuccessfulResult_flowShouldEmitAResponse() {
        with(spy<SingleModalityFlowBaseImpl>()) {
            responsesEmitter = mock()
            nextIntentEmitter = mock()

            handleIntentResponse(REQUEST_CODE_FOR_SINGLE_MODALITY_FLOW, Activity.RESULT_OK, mock())

            verifyOnce(this) { completeWithValidResponse(anyNotNull()) }
        }
    }

    @Test
    fun givenSingleModalityFlow_receivesAnResultForDifferentModality_flowShouldIgnoreIt() {
        with(spy<SingleModalityFlowBaseImpl>()) {
            responsesEmitter = mock()
            nextIntentEmitter = mock()

            handleIntentResponse(REQUEST_CODE_FOR_SINGLE_MODALITY_FLOW + 1, Activity.RESULT_OK, mock())

            verifyNever(this) { completeWithValidResponse(anyNotNull()) }
        }
    }

    @Test
    fun givenSingleModalityFlow_receivesAMalformedResult_shouldThrowAnError() {
        with(spy(SingleModalityFlowBaseImpl())) {
            whenever(this) { intentRequestCode } thenReturn REQUEST_CODE_FOR_SINGLE_MODALITY_FLOW
            whenever(this) { getModalityStepRequests() } thenReturn mock()
            whenever(this) { extractModalityResponse(any(), any(), any()) } thenThrow RuntimeException("Malformed data")
            responsesEmitter = mock()
            nextIntentEmitter = mock()

            handleIntentResponse(REQUEST_CODE_FOR_SINGLE_MODALITY_FLOW, Activity.RESULT_OK, Intent())

            verifyNever(this) { completeWithValidResponse(anyNotNull()) }
            verifyOnce(this) { completeWithAnError(anyNotNull()) }
        }
    }


    class SingleModalityFlowBaseImpl : SingleModalityFlowBase() {
        override val intentRequestCode: Int = REQUEST_CODE_FOR_SINGLE_MODALITY_FLOW
        override fun getModalityStepRequests(): ModalityStepRequest = mock()
        override fun extractModalityResponse(requestCode: Int, resultCode: Int, data: Intent?): ModalityResponse = mock()
    }
}

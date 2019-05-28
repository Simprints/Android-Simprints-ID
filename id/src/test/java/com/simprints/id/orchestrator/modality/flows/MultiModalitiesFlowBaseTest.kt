package com.simprints.id.orchestrator.modality.flows

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.anyOrNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class MultiModalitiesFlowBaseTest {

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()
    }

    @Test
    fun givenMultiModalitiesFlow_itShouldGenerateMultipleModalityStepRequests() {
        val singleModality: ModalityFlow = mock()
        whenever { singleModality.modalityResponses } thenReturn Observable.just(mock())
        whenever { singleModality.modalityStepRequests } thenReturn Observable.just(mock())
        val multiModalitiesFlow = MultiModalitiesFlowBase(listOf(singleModality, singleModality))

        val testObserver = multiModalitiesFlow.modalityStepRequests.test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(2)
    }


    @Test
    fun givenMultiModalitiesFlow_itShouldGenerateMultipleModalityResponses() {
        val singleModality: ModalityFlow = mock()
        whenever { singleModality.modalityResponses } thenReturn Observable.just(mock())
        whenever { singleModality.modalityStepRequests } thenReturn Observable.just(mock())
        val multiModalitiesFlow = MultiModalitiesFlowBase(listOf(singleModality, singleModality))

        val testObserver = multiModalitiesFlow.modalityResponses.test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(2)
    }

    @Test
    fun givenMultiModalitiesFlow_itShouldForwardIntentResultsToSingleModalities() {
        val singleModality: ModalityFlow = mock()
        whenever { singleModality.modalityResponses } thenReturn Observable.just(mock())
        whenever { singleModality.modalityStepRequests } thenReturn Observable.just(mock())
        whenever { singleModality.handleIntentResponse(anyNotNull(), anyNotNull(), anyOrNull()) } thenReturn true
        val multiModalitiesFlow = MultiModalitiesFlowBase(listOf(singleModality, singleModality))

        val resultHandled = multiModalitiesFlow.handleIntentResponse(0, 0, Intent())
        assertThat(resultHandled).isTrue()
    }
}

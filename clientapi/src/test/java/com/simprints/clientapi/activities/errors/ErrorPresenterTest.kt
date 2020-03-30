package com.simprints.clientapi.activities.errors

import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class ErrorPresenterTest {

    private val view = mockk<ErrorActivity>()

    @Before
    fun setup() {
        BaseUnitTestConfig()
            .rescheduleRxMainThread()
            .coroutinesMainThread()
    }


    @Test
    fun start_shouldSetCorrectErrorMessage() {
        runBlockingTest {
            val clientApiSessionEventsManagerMock = mockk<ClientApiSessionEventsManager>(relaxed = true)

            ErrorPresenter(view, clientApiSessionEventsManagerMock).apply {
                start(ClientApiAlert.INVALID_CLIENT_REQUEST)
            }

            verify(exactly = 1) { view.setErrorMessageText(any()) }
        }
    }

    @Test
    fun handleCloseClick_ShouldTellTheViewToClose() {
        runBlocking {
            ErrorPresenter(view, mockk()).apply {
                start()
                handleCloseOrBackClick()
            }

            verify(exactly = 1) { view.closeActivity() }
        }
    }
}

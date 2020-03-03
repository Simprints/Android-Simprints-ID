package com.simprints.clientapi.activities.errors

import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class ErrorPresenterTest {

    private val view = mock<ErrorActivity>()

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

            verifyOnce(view) { setErrorMessageText(anyNotNull()) }
        }
    }

    @Test
    fun handleCloseClick_ShouldTellTheViewToClose() {
        runBlocking {
            ErrorPresenter(view, mock()).apply {
                start()
                handleCloseOrBackClick()
            }
            verifyOnce(view) { closeActivity() }
        }
    }
}

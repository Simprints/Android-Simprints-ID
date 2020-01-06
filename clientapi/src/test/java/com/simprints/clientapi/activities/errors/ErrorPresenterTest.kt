package com.simprints.clientapi.activities.errors

import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.reactivex.Completable
import kotlinx.coroutines.*
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
        val clientApiSessionEventsManagerMock = mock<ClientApiSessionEventsManager>().apply {
            whenever(this) { addAlertScreenEvent(anyNotNull()) } thenReturn Completable.complete()
        }

        ErrorPresenter(view, clientApiSessionEventsManagerMock).apply {
            start(ClientApiAlert.INVALID_CLIENT_REQUEST)
        }

        verifyOnce(view) { setErrorMessageText(anyNotNull()) }
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

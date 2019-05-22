package com.simprints.clientapi.activities.errors

import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import org.junit.Test
import com.simprints.id.R as Rid

class ErrorPresenterTest {

    private val view = mock<ErrorActivity>()

    @Test
    fun start_shouldSetCorrectErrorMessage() {
        ErrorPresenter(view, mock(), mock()).apply {
            start(ClientApiAlert.INVALID_CLIENT_REQUEST)
        }
        verifyOnce(view) { setErrorMessageText(anyNotNull()) }
    }

    @Test
    fun handleCloseClick_ShouldTellTheViewToClose() {
        ErrorPresenter(view, mock(), mock()).apply { start(); handleCloseClick() }
        verifyOnce(view) { closeActivity() }
    }
}

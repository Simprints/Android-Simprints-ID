package com.simprints.clientapi.activities.errors

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorPresenterTest {

    companion object {
        private const val TEST_ERROR_MESSAGE = "TEST ERROR MESSAGE"
    }

    private val view = mock<ErrorActivity>()

    @Test
    fun start_shouldSetCorrectErrorMessage() {
        ErrorPresenter(view, TEST_ERROR_MESSAGE).apply { start() }
        verifyOnce(view) { setErrorMessageText(TEST_ERROR_MESSAGE) }
    }

    @Test
    fun handleCloseClick_ShouldTellTheViewToClose() {
        ErrorPresenter(view, TEST_ERROR_MESSAGE).apply { start(); handleCloseClick() }
        verifyOnce(view) { closeActivity() }
    }
}

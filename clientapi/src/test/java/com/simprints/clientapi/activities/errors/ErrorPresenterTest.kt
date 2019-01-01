package com.simprints.clientapi.activities.errors

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ErrorPresenterTest {

    companion object {
        private const val TEST_ERROR_MESSAGE = "TEST ERROR MESSAGE"
    }

    @Mock
    private val view: ErrorContract.View = ErrorActivity()

    @Test
    fun start_shouldSetCorrectErrorMessage() {
        ErrorPresenter(view, TEST_ERROR_MESSAGE).apply { start() }
        Mockito.verify(view, Mockito.times(1)).setErrorMessageText(TEST_ERROR_MESSAGE)
    }

    @Test
    fun handleCloseClick_ShouldTellTheViewToClose() {
        ErrorPresenter(view, TEST_ERROR_MESSAGE).apply { start(); handleCloseClick() }
        Mockito.verify(view, Mockito.times(1)).closeActivity()
    }
}

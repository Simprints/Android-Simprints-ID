package com.simprints.core.tools.exceptions

import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class AppCoroutineExceptionHandlerTest {
    @Test
    fun `calling handleException logs it to Simber`() {
        val appCoroutineExceptionHandler = AppCoroutineExceptionHandler()
        val coroutineContext: CoroutineContext = mockk()
        val exception = Exception()

        mockkObject(Simber)

        appCoroutineExceptionHandler.handleException(coroutineContext, exception)
        verify { Simber.e(any(), exception, any<CrashReportTag>()) }

        unmockkObject(Simber)
    }
}

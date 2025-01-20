package com.simprints.core.tools.exceptions

import com.simprints.infra.logging.Simber
import io.mockk.every
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

        val simber = mockk<Simber>(relaxed = true) {
            every { tag(any()) } returns this
        }
        mockkObject(Simber)
        every { Simber.INSTANCE } returns simber

        appCoroutineExceptionHandler.handleException(coroutineContext, exception)
        verify { simber.e(any(), exception) }

        unmockkObject(Simber)
    }
}

package com.simprints.infra.logging

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class LogDirectoryProviderTest {
    private val logDirectoryProvider = LogDirectoryProvider()

    @Test
    fun `creates directories if not existing`() {
        val context = mockk<Context> {
            every { filesDir } returns File("logtestpath")
        }

        val result = logDirectoryProvider(context)
        assertEquals("logtestpath/logs", result.path)
    }
}

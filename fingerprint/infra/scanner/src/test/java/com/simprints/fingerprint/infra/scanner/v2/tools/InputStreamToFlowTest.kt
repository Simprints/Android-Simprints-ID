package com.simprints.fingerprint.infra.scanner.v2.tools
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InputStreamToFlowTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    fun `asFlow emits full buffer`() = runTest {
        val inputStream = mockk<InputStream>()
        val bufferSlot = slot<ByteArray>()
        every { inputStream.read(capture(bufferSlot)) } returnsMany listOf(1024, 1024, -1)

        val result = inputStream.asFlow(dispatcher).toList()

        assertEquals(2, result.size)
        assertTrue(result.all { it.size == 1024 })
    }

    @Test
    fun `asFlow emits remaining bytes when stream ends`() = runTest {
        val inputStream = mockk<InputStream>()
        val bufferSlot = slot<ByteArray>()
        every { inputStream.read(capture(bufferSlot)) } answers {
            val buffer = bufferSlot.captured
            buffer.fill(1, 0, 512)
            512
        } andThen -1

        val result = inputStream.asFlow(dispatcher).toList()

        assertEquals(1, result.size)
        assertEquals(512, result[0].size)
    }

    @Test
    fun `asFlow handles IOException gracefully`() = runTest {
        val inputStream = mockk<InputStream>()
        every { inputStream.read(any()) } throws IOException("Test exception")

        val result = inputStream.asFlow(dispatcher).toList()

        // Expect no emissions due to exception
        assertTrue(result.isEmpty())
    }

    @Test
    fun `asFlow stops emitting when stream ends`() = runTest {
        val inputStream = mockk<InputStream>()
        every { inputStream.read(any()) } returns -1

        val result = inputStream.asFlow(dispatcher).toList()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `asFlow handles empty stream correctly`() = runTest {
        val inputStream = mockk<InputStream>()
        every { inputStream.read(any()) } returns -1

        val result = inputStream.asFlow(dispatcher).toList()

        assertTrue(result.isEmpty())
    }
}

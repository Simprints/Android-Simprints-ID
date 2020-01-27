package com.simprints.fingerprintscanner.v2.tools.reactive

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.testtools.calculateNumberOfElements
import com.simprints.fingerprintscanner.testtools.toHexStrings
import com.simprints.fingerprintscanner.v2.tools.primitives.chunked
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class RxInputStreamTest {

    @Test
    fun inputStreamToFlowable_writingByteArraysUnderBufferAndClosing_combinesItemsOnNextAndThenCompletes() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val writeSize = 4
        val bufferSize = 1024

        val testSubscriber = inputStream
            .toFlowable(bufferSize = bufferSize)
            .testSubscribe()

        val bytes = "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF ".hexToByteArray()
        bytes.chunked(writeSize).forEach { outputStream.write(it) }
        outputStream.close()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.valueCount()).isEqualTo(calculateNumberOfElements(bufferSize, bytes.size))
        assertThat(testSubscriber.values().toHexStrings())
            .containsExactlyElementsIn(bytes.chunked(bufferSize).toHexStrings())
            .inOrder()
    }

    @Test
    fun inputStreamToFlowable_writingByteArraysOverBufferAndClosing_emitsMultipleItemsOnNextAndThenCompletes() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val writeSize = 4
        val bufferSize = 12

        val testSubscriber = inputStream
            .toFlowable(bufferSize = bufferSize)
            .testSubscribe()

        val bytes = "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF ".hexToByteArray()
        bytes.chunked(writeSize).forEach { outputStream.write(it) }
        outputStream.close()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.valueCount()).isEqualTo(calculateNumberOfElements(bufferSize, bytes.size))
        assertThat(testSubscriber.values().toHexStrings())
            .containsExactlyElementsIn(bytes.chunked(bufferSize).toHexStrings())
            .inOrder()
    }
}

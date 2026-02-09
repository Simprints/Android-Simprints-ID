package com.simprints.fingerprint.infra.scanner.v2.scanner.ota.cypress

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands.SendImageChunk
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses.ContinueResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses.ErrorResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses.OkResponse
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.cypressota.CypressOtaMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.crc.Crc32Calculator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

class CypressOtaControllerTest {
    @Test
    fun program_correctlyEmitsProgressValuesAndCompletes() = runTest {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val testObserver = cypressOtaController
            .program(
                configureMessageStreamMock(),
                binFile,
            ).toList()

        assertThat(testObserver.toList())
            .containsExactlyElementsIn(
                generateExpectedProgressValues(
                    binFile,
                ),
            ).inOrder()
    }

    @Test
    fun program_correctlyCallsComputeCrcAndSendCorrectNumberOfTimes() = runTest {
        val binFile = generateRandomBinFile()
        val expectedNumberOfCalls = expectedNumberOfChunks(binFile) + 3

        val crc32Calculator = configureCrcCalculatorMock()
        val messageStreamMock = configureMessageStreamMock()
        val cypressOtaController = CypressOtaController(crc32Calculator)

        cypressOtaController.program(messageStreamMock, binFile).toList()

        verify { crc32Calculator.calculateCrc32(any()) }
        coVerify(exactly = expectedNumberOfCalls) { messageStreamMock.outgoing.sendMessage(any()) }
    }

    @Test(expected = OtaFailedException::class)
    fun program_receivesErrorAtPrepareDownload_throwsException() = runTest {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        cypressOtaController.program(
            configureMessageStreamMock(errorPositions = listOf(0)),
            generateRandomBinFile(),
        )
    }

    @Test(expected = OtaFailedException::class)
    fun program_receivesErrorAtDownload_throwsException() = runTest {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())
        cypressOtaController.program(
            configureMessageStreamMock(errorPositions = listOf(1)),
            generateRandomBinFile(),
        )
    }

    @Test(expected = OtaFailedException::class)
    fun program_receivesErrorDuringSendImageProcess_emitsValueUntilErrorThenThrowsException() = runTest {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val progressValues = generateExpectedProgressValues(binFile)
        val testObserver = cypressOtaController.program(
            configureMessageStreamMock(errorPositions = listOf(4)),
            binFile,
        )
        assertThat(testObserver.toList()).containsExactlyElementsIn(progressValues.slice(0..1)).inOrder()
    }

    @Test(expected = OtaFailedException::class)
    fun program_receivesErrorAtVerify_throwsException() = runTest {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val indexOfVerifyResponse = expectedNumberOfChunks(binFile) + 2
        cypressOtaController
            .program(
                configureMessageStreamMock(errorPositions = listOf(indexOfVerifyResponse)),
                binFile,
            ).toList()
    }

    private fun configureCrcCalculatorMock() = mockk<Crc32Calculator> {
        every { calculateCrc32(any()) } returns 42
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.configureMessageStreamMock(errorPositions: List<Int> = listOf()): CypressOtaMessageChannel {
        val messageIndex = AtomicInteger(0)
        var readyToRead = false
        var desirableResponse: CypressOtaResponse = ContinueResponse()
        val inputStream = mockk<CypressOtaMessageInputStream> {
            justRun { connect(any()) }
            every { cypressOtaResponseStream } answers {
                while (!readyToRead) {
                    advanceTimeBy(SMALL_DELAY)
                }
                readyToRead = false
                flowOf(
                    if (errorPositions.contains(messageIndex.getAndIncrement())) {
                        ErrorResponse()
                    } else {
                        desirableResponse
                    },
                )
            }
        }
        val outputStream = mockk<CypressOtaMessageOutputStream> {
            coEvery { sendMessage(any()) } answers {
                desirableResponse = when (args[0] as CypressOtaCommand) {
                    is SendImageChunk -> ContinueResponse()
                    else -> OkResponse()
                }
                readyToRead = true
            }
        }
        return CypressOtaMessageChannel(
            inputStream,
            outputStream,
            Dispatchers.IO,
        )
    }

    companion object {
        private fun generateRandomBinFile() = Random.nextBytes(1200 + Random.nextInt(2000))

        private fun expectedNumberOfChunks(binFile: ByteArray): Int = (ceil((binFile.size.toFloat() - 16f) / 253f) + 1f).roundToInt()

        private fun generateExpectedProgressValues(binFile: ByteArray): List<Float> {
            val numberOfChunks = expectedNumberOfChunks(binFile)
            return (1..numberOfChunks).map { it.toFloat() / numberOfChunks.toFloat() }
        }

        private const val SMALL_DELAY = 1L
    }
}

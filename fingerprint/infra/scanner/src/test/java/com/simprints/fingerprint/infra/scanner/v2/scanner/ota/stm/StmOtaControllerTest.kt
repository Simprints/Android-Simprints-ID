package com.simprints.fingerprint.infra.scanner.v2.scanner.ota.stm

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
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

class StmOtaControllerTest {
    @Test
    fun program_correctlyEmitsProgressValuesAndCompletes() = runTest {
        val stmOtaController = StmOtaController()

        val firmwareBin = generateRandomBinFile()
        val expectedProgress = generateExpectedProgressValues(firmwareBin)

        val testObserver =
            stmOtaController.program(configureMessageStreamMock(), firmwareBin).toList()
        assertThat(testObserver.toList()).containsExactlyElementsIn(expectedProgress).inOrder()
    }

    @Test
    fun program_correctlyCallsParseAndSendCorrectNumberOfTimes() = runTest {
        val firmwareBin = generateRandomBinFile()
        val expectedNumberOfCalls = expectedNumberOfChunks(firmwareBin) * 3 + 5

        val messageStreamMock = configureMessageStreamMock()
        val stmOtaController = StmOtaController()
        stmOtaController.program(messageStreamMock, firmwareBin).toList()
        coVerify(exactly = expectedNumberOfCalls) { messageStreamMock.outgoing.sendMessage(any()) }
    }

    @Test(expected = OtaFailedException::class)
    fun program_receivesNackAtStart_throwsException() = runTest {
        val stmOtaController = StmOtaController()
        stmOtaController
            .program(
                configureMessageStreamMock(nackPositions = listOf(0)),
                byteArrayOf(),
            ).toList()
    }

    @Test(expected = OtaFailedException::class)
    fun program_receivesNackDuringProcess_emitsValueUntilNackThenThrowsException() = runTest {
        val stmOtaController = StmOtaController()

        val firmwareBin = generateRandomBinFile()
        val expectedProgress = generateExpectedProgressValues(firmwareBin)

        val testObserver = stmOtaController
            .program(
                configureMessageStreamMock(nackPositions = listOf(10)),
                firmwareBin,
            ).toList()
        assertThat(testObserver.toList())
            .containsExactlyElementsIn(expectedProgress.slice(0..1))
            .inOrder()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.configureMessageStreamMock(nackPositions: List<Int> = listOf()): StmOtaMessageChannel {
        val messageIndex = AtomicInteger(0)
        var readyToRead = false
        var response: StmOtaResponse = CommandAcknowledgement(CommandAcknowledgement.Kind.ACK)
        val incoming = mockk<StmOtaMessageInputStream> {
            justRun { connect(any()) }
            every { stmOtaResponseStream } answers {
                while (!readyToRead) {
                    advanceTimeBy(SMALL_DELAY)
                }
                readyToRead = false
                flowOf(response)
            }
        }
        val outgoing = mockk<StmOtaMessageOutputStream> {
            coEvery { sendMessage(any()) } answers {
                response = CommandAcknowledgement(
                    if (nackPositions.contains(messageIndex.getAndIncrement())) {
                        CommandAcknowledgement.Kind.NACK
                    } else {
                        CommandAcknowledgement.Kind.ACK
                    },
                )
                readyToRead = true
            }
        }
        return StmOtaMessageChannel(
            incoming,
            outgoing,
            Dispatchers.IO,
        )
    }

    companion object {
        private fun generateRandomBinFile() = Random.nextBytes(1200 + Random.nextInt(2000))

        private fun expectedNumberOfChunks(binFile: ByteArray): Int = ceil(binFile.size.toFloat() / 256f).roundToInt()

        private fun generateExpectedProgressValues(binFile: ByteArray): List<Float> {
            val numberOfChunks = expectedNumberOfChunks(binFile)
            return (1..numberOfChunks).map { it.toFloat() / numberOfChunks.toFloat() }
        }

        private const val SMALL_DELAY = 1L
    }
}

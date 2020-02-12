package com.simprints.fingerprintscanner.v2.scanner.ota.cypress

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.commands.SendImageChunk
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.ContinueResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.ErrorResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.OkResponse
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
import com.simprints.fingerprintscanner.v2.tools.crc.Crc32Calculator
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

class CypressOtaControllerTest {

    private val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.NONE)

    @Test
    fun program_correctlyEmitsProgressValuesAndCompletes() {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val testObserver = cypressOtaController.program(configureMessageStreamMock(), responseErrorHandler, binFile).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(generateExpectedProgressValues(binFile)).inOrder()
        testObserver.assertComplete()
    }

    @Test
    fun program_correctlyCallsComputeCrcAndSendCorrectNumberOfTimes() {
        val binFile = generateRandomBinFile()
        val expectedNumberOfCalls = expectedNumberOfChunks(binFile) + 3

        val crc32Calculator = configureCrcCalculatorMock()
        val messageStreamMock = configureMessageStreamMock()
        val cypressOtaController = CypressOtaController(crc32Calculator)

        val testObserver = cypressOtaController.program(messageStreamMock, responseErrorHandler, binFile).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        verifyOnce(crc32Calculator) { calculateCrc32(anyNotNull()) }
        verifyExactly(expectedNumberOfCalls, messageStreamMock.outgoing) { sendMessage(anyNotNull()) }
    }

    @Test
    fun program_receivesErrorAtPrepareDownload_throwsException() {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        val testObserver = cypressOtaController.program(
            configureMessageStreamMock(errorPositions = listOf(0)), responseErrorHandler, generateRandomBinFile()).testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(OtaFailedException::class.java)
    }

    @Test
    fun program_receivesErrorAtDownload_throwsException() {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        val testObserver = cypressOtaController.program(
            configureMessageStreamMock(errorPositions = listOf(1)), responseErrorHandler, generateRandomBinFile()).testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(OtaFailedException::class.java)
    }

    @Test
    fun program_receivesErrorDuringSendImageProcess_emitsValueUntilErrorThenThrowsException() {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val progressValues = generateExpectedProgressValues(binFile)
        val testObserver = cypressOtaController.program(
            configureMessageStreamMock(errorPositions = listOf(4)), responseErrorHandler, binFile).testSubscribe()

        testObserver.awaitTerminalEvent()
        assertThat(testObserver.values()).containsExactlyElementsIn(progressValues.slice(0..1)).inOrder()
        testObserver.assertError(OtaFailedException::class.java)
    }

    @Test
    fun program_receivesErrorAtVerify_throwsException() {
        val cypressOtaController = CypressOtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val indexOfVerifyResponse = expectedNumberOfChunks(binFile) + 2
        val testObserver = cypressOtaController.program(
            configureMessageStreamMock(errorPositions = listOf(indexOfVerifyResponse)), responseErrorHandler, binFile).testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(OtaFailedException::class.java)
    }

    private fun configureCrcCalculatorMock() = setupMock<Crc32Calculator> {
        whenThis { calculateCrc32(anyNotNull()) } thenReturn 42
    }

    private fun configureMessageStreamMock(errorPositions: List<Int> = listOf()): CypressOtaMessageChannel {
        val responseSubject = PublishSubject.create<CypressOtaResponse>()
        val messageIndex = AtomicInteger(0)

        return CypressOtaMessageChannel(
            spy(CypressOtaMessageInputStream(mock())).apply {
                whenThis { connect(anyNotNull()) } thenDoNothing {}
                cypressOtaResponseStream = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            },
            setupMock {
                whenThis { sendMessage(anyNotNull()) } then {
                    val desirableResponse = when (it.arguments[0] as CypressOtaCommand) {
                        is SendImageChunk -> ContinueResponse()
                        else -> OkResponse()
                    }
                    Completable.complete().doAfterTerminate {
                        responseSubject.onNext(
                            if (errorPositions.contains(messageIndex.getAndIncrement())) {
                                ErrorResponse()
                            } else {
                                desirableResponse
                            }
                        )
                    }
                }
            }
        )
    }

    companion object {
        private fun generateRandomBinFile() = Random.nextBytes(1200 + Random.nextInt(2000))

        private fun expectedNumberOfChunks(binFile: ByteArray): Int =
            (ceil((binFile.size.toFloat() - 16f) / 253f) + 1f).roundToInt()

        private fun generateExpectedProgressValues(binFile: ByteArray): List<Float> {
            val numberOfChunks = expectedNumberOfChunks(binFile)
            return (1..numberOfChunks).map { it.toFloat() / numberOfChunks.toFloat() }
        }
    }
}

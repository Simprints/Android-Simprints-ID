package com.simprints.fingerprintscanner.v2.scanner.ota.un20

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.channel.MainMessageChannel
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.StartOtaCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.VerifyOtaCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.WriteOtaChunkCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.StartOtaResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.VerifyOtaResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.WriteOtaChunkResponse
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
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

class Un20OtaControllerTest {

    private val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.None)

    @Test
    fun program_correctlyEmitsProgressValuesAndCompletes() {
        val un20OtaController = Un20OtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val testObserver = un20OtaController.program(configureMessageStreamMock(), responseErrorHandler, binFile).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(generateExpectedProgressValues(binFile)).inOrder()
        testObserver.assertComplete()
    }

    @Test
    fun program_correctlyCallsComputeCrcAndSendCorrectNumberOfTimes() {
        val binFile = generateRandomBinFile()
        val expectedNumberOfCalls = expectedNumberOfChunks(binFile) + 2

        val crc32Calculator = configureCrcCalculatorMock()
        val messageStreamMock = configureMessageStreamMock()
        val un20OtaController = Un20OtaController(crc32Calculator)

        val testObserver = un20OtaController.program(messageStreamMock, responseErrorHandler, binFile).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        verifyOnce(crc32Calculator) { calculateCrc32(anyNotNull()) }
        verifyExactly(expectedNumberOfCalls, messageStreamMock.outgoing) { sendMessage(anyNotNull()) }
    }

    @Test
    fun program_receivesErrorAtPrepareDownload_throwsException() {
        val un20OtaController = Un20OtaController(configureCrcCalculatorMock())

        val testObserver = un20OtaController.program(
            configureMessageStreamMock(errorPositions = listOf(0)), responseErrorHandler, generateRandomBinFile()).testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(OtaFailedException::class.java)
    }

    @Test
    fun program_receivesErrorAtDownload_throwsException() {
        val un20OtaController = Un20OtaController(configureCrcCalculatorMock())

        val testObserver = un20OtaController.program(
            configureMessageStreamMock(errorPositions = listOf(1)), responseErrorHandler, generateRandomBinFile()).testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(OtaFailedException::class.java)
    }

    @Test
    fun program_receivesErrorDuringSendImageProcess_emitsValueUntilErrorThenThrowsException() {
        val un20OtaController = Un20OtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val progressValues = generateExpectedProgressValues(binFile)
        val testObserver = un20OtaController.program(
            configureMessageStreamMock(errorPositions = listOf(4)), responseErrorHandler, binFile).testSubscribe()

        testObserver.awaitTerminalEvent()
        assertThat(testObserver.values()).containsExactlyElementsIn(progressValues.slice(0..1)).inOrder()
        testObserver.assertError(OtaFailedException::class.java)
    }

    @Test
    fun program_receivesErrorAtVerify_throwsException() {
        val un20OtaController = Un20OtaController(configureCrcCalculatorMock())

        val binFile = generateRandomBinFile()
        val indexOfVerifyResponse = expectedNumberOfChunks(binFile) + 1
        val testObserver = un20OtaController.program(
            configureMessageStreamMock(errorPositions = listOf(indexOfVerifyResponse)), responseErrorHandler, binFile).testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(OtaFailedException::class.java)
    }

    private fun configureCrcCalculatorMock() = setupMock<Crc32Calculator> {
        whenThis { calculateCrc32(anyNotNull()) } thenReturn 42
    }

    private fun configureMessageStreamMock(errorPositions: List<Int> = listOf()): MainMessageChannel {
        val responseSubject = PublishSubject.create<Un20Response>()
        val messageIndex = AtomicInteger(0)

        return MainMessageChannel(
            spy(MainMessageInputStream(mock(), mock(), mock(), mock())).apply {
                whenThis { connect(anyNotNull()) } thenDoNothing {}
                un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            },
            setupMock {
                whenThis { sendMessage(anyNotNull()) } then {
                    val resultCode =
                        if (errorPositions.contains(messageIndex.getAndIncrement())) {
                            OperationResultCode.UNKNOWN_ERROR
                        } else {
                            OperationResultCode.OK
                        }
                    val response = when (it.arguments[0] as Un20Command) {
                        is StartOtaCommand -> StartOtaResponse(resultCode)
                        is WriteOtaChunkCommand -> WriteOtaChunkResponse(resultCode)
                        is VerifyOtaCommand -> VerifyOtaResponse(resultCode)
                        else -> null
                    }
                    response?.let {
                        Completable.complete().doAfterTerminate {
                            responseSubject.onNext(it)
                        }
                    } ?: Completable.complete()
                }
            }
        )
    }

    companion object {
        private fun generateRandomBinFile() = Random.nextBytes(350000 + Random.nextInt(200000))

        private fun expectedNumberOfChunks(binFile: ByteArray): Int =
            ceil(binFile.size.toFloat() / Un20OtaController.MAX_UN20_OTA_CHUNK_SIZE).roundToInt()

        private fun generateExpectedProgressValues(binFile: ByteArray): List<Float> {
            val numberOfChunks = expectedNumberOfChunks(binFile)
            return (1..numberOfChunks).map { it.toFloat() / numberOfChunks.toFloat() }
        }
    }
}

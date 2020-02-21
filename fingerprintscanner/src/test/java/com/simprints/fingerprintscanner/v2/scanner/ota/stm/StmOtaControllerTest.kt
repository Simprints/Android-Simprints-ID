package com.simprints.fingerprintscanner.v2.scanner.ota.stm

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
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

class StmOtaControllerTest {

    private val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.NONE)

    @Test
    fun program_correctlyEmitsProgressValuesAndCompletes() {
        val stmOtaController = StmOtaController()

        val firmwareBin = generateRandomBinFile()
        val expectedProgress = generateExpectedProgressValues(firmwareBin)

        val testObserver = stmOtaController.program(configureMessageStreamMock(), responseErrorHandler, firmwareBin).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedProgress).inOrder()
        testObserver.assertComplete()
    }

    @Test
    fun program_correctlyCallsParseAndSendCorrectNumberOfTimes() {
        val firmwareBin = generateRandomBinFile()
        val expectedNumberOfCalls = expectedNumberOfChunks(firmwareBin) * 3 + 5

        val messageStreamMock = configureMessageStreamMock()
        val stmOtaController = StmOtaController()

        val testObserver = stmOtaController.program(messageStreamMock, responseErrorHandler, firmwareBin).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        verifyExactly(expectedNumberOfCalls, messageStreamMock.outgoing) { sendMessage(anyNotNull()) }
    }

    @Test
    fun program_receivesNackAtStart_throwsException() {
        val stmOtaController = StmOtaController()

        val testObserver = stmOtaController.program(
            configureMessageStreamMock(nackPositions = listOf(0)), responseErrorHandler, byteArrayOf()).testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(OtaFailedException::class.java)
    }

    @Test
    fun program_receivesNackDuringProcess_emitsValueUntilNackThenThrowsException() {
        val stmOtaController = StmOtaController()

        val firmwareBin = generateRandomBinFile()
        val expectedProgress = generateExpectedProgressValues(firmwareBin)

        val testObserver = stmOtaController.program(
            configureMessageStreamMock(nackPositions = listOf(10)), responseErrorHandler, firmwareBin).testSubscribe()

        testObserver.awaitTerminalEvent()
        assertThat(testObserver.values()).containsExactlyElementsIn(expectedProgress.slice(0..1)).inOrder()
        testObserver.assertError(OtaFailedException::class.java)
    }

    private fun configureMessageStreamMock(nackPositions: List<Int> = listOf()): StmOtaMessageChannel {
        val responseSubject = PublishSubject.create<StmOtaResponse>()
        val messageIndex = AtomicInteger(0)

        return StmOtaMessageChannel(
            spy(StmOtaMessageInputStream(mock())).apply {
                whenThis { connect(anyNotNull()) } thenDoNothing {}
                stmOtaResponseStream = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            },
            setupMock {
                whenThis { sendMessage(anyNotNull()) } then {
                    Completable.complete().doAfterTerminate {
                        responseSubject.onNext(CommandAcknowledgement(
                            if (nackPositions.contains(messageIndex.getAndIncrement())) {
                                CommandAcknowledgement.Kind.NACK
                            } else {
                                CommandAcknowledgement.Kind.ACK
                            }
                        ))
                    }
                }
            }
        )
    }

    companion object {
        private fun generateRandomBinFile() = Random.nextBytes(1200 + Random.nextInt(2000))

        private fun expectedNumberOfChunks(binFile: ByteArray): Int =
            ceil(binFile.size.toFloat() / 256f).roundToInt()

        private fun generateExpectedProgressValues(binFile: ByteArray): List<Float> {
            val numberOfChunks = expectedNumberOfChunks(binFile)
            return (1..numberOfChunks).map { it.toFloat() / numberOfChunks.toFloat() }
        }
    }
}

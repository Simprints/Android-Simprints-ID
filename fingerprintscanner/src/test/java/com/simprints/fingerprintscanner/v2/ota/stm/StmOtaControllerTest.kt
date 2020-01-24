package com.simprints.fingerprintscanner.v2.ota.stm

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.stream.StmOtaMessageStream
import com.simprints.fingerprintscanner.v2.tools.hexparser.FirmwareByteChunk
import com.simprints.fingerprintscanner.v2.tools.hexparser.IntelHexParser
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class StmOtaControllerTest {


    @Test
    fun program_correctlyEmitsProgressValuesAndCompletes() {
        val stmOtaController = StmOtaController(configureIntelHexParserMock())

        val testObserver = stmOtaController.program(configureMessageStreamMock(), "").testSubscribe()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(PROGRESS_VALUES).inOrder()
        testObserver.assertComplete()
    }

    @Test
    fun program_correctlyCallsParseAndSendCorrectNumberOfTimes() {
        val expectedNumberOfCalls = FIRMWARE_BYTE_CHUNKS.size * 3

        val intelHexParserMock = configureIntelHexParserMock()
        val messageStreamMock = configureMessageStreamMock()
        val stmOtaController = StmOtaController(intelHexParserMock)

        val testObserver = stmOtaController.program(messageStreamMock, "").testSubscribe()

        testObserver.awaitAndAssertSuccess()

        verifyOnce(intelHexParserMock) { parse(anyNotNull()) }
        verifyExactly(expectedNumberOfCalls, messageStreamMock.outgoing) { sendMessage(anyNotNull()) }
    }

    @Test
    fun program_hexParseFails_propagatesError() {
        val intelHexParserMock = setupMock<IntelHexParser> {
            whenThis { parse(anyNotNull()) } thenThrow IllegalArgumentException()
        }
        val stmOtaController = StmOtaController(intelHexParserMock)

        val testObserver = stmOtaController.program(configureMessageStreamMock(), "").testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun program_receivesNackAtStart_throwsException() {
        val stmOtaController = StmOtaController(configureIntelHexParserMock())

        val testObserver = stmOtaController.program(
            configureMessageStreamMock(nackPositions = listOf(0)), "").testSubscribe()

        testObserver.awaitTerminalEvent()
        testObserver.assertError(NotImplementedError::class.java) // TODO : Exception handling
    }

    @Test
    fun program_receivesNackDuringProcess_emitsValueUntilNackThenthrowsException() {
        val stmOtaController = StmOtaController(configureIntelHexParserMock())

        val testObserver = stmOtaController.program(
            configureMessageStreamMock(nackPositions = listOf(7)), "").testSubscribe()

        testObserver.awaitTerminalEvent()
        assertThat(testObserver.values()).containsExactlyElementsIn(PROGRESS_VALUES.slice(0..1)).inOrder()
        testObserver.assertError(NotImplementedError::class.java) // TODO : Exception handling
    }

    private fun configureIntelHexParserMock() = setupMock<IntelHexParser> {
        whenThis { parse(anyNotNull()) } thenReturn FIRMWARE_BYTE_CHUNKS
    }

    private fun configureMessageStreamMock(nackPositions: List<Int> = listOf()): StmOtaMessageStream {
        val responseSubject = PublishSubject.create<StmOtaResponse>()
        val messageIndex = AtomicInteger(0)

        return StmOtaMessageStream(
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
        val FIRMWARE_BYTE_CHUNKS = listOf(
            FirmwareByteChunk("FF000100".hexToByteArray(), "214601360121470136007EFE09D21901".hexToByteArray()),
            FirmwareByteChunk("FF000110".hexToByteArray(), "2146017E17C20001FF5F160021480119".hexToByteArray()),
            FirmwareByteChunk("FF000120".hexToByteArray(), "194E79234623965778239EDA3F01B2CA".hexToByteArray()),
            FirmwareByteChunk("FFFF0130".hexToByteArray(), "3F0156702B5E712B722B732146013421".hexToByteArray())
        )
        val PROGRESS_VALUES = listOf(0.25f, 0.50f, 0.75f, 1.00f)
    }
}

package com.simprints.fingerprintscanner.v2.scanner

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.isA
import com.simprints.fingerprintscanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprintscanner.v2.channel.MainMessageChannel
import com.simprints.fingerprintscanner.v2.channel.RootMessageChannel
import com.simprints.fingerprintscanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprintscanner.v2.domain.Mode
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.CaptureFingerprintCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.GetImageCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.GetTemplateCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageFormat
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateData
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.GetImageResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.GetTemplateResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.SetSmileLedStateCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses.SetSmileLedStateResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterCypressOtaModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterStmOtaModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterCypressOtaModeResponse
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterStmOtaModeResponse
import com.simprints.fingerprintscanner.v2.exceptions.state.IllegalUn20StateException
import com.simprints.fingerprintscanner.v2.exceptions.state.IncorrectModeException
import com.simprints.fingerprintscanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.cypressota.CypressOtaMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
import com.simprints.fingerprintscanner.v2.scanner.ota.cypress.CypressOtaController
import com.simprints.fingerprintscanner.v2.scanner.ota.stm.StmOtaController
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

class ScannerTest {

    private val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.None)

    @Test
    fun scanner_callEnterModeBeforeConnect_throwsException() {
        val scanner = Scanner(mock(), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.enterMainMode().testSubscribe().await().assertError(NotConnectedException::class.java)
    }

    @Test
    fun scanner_connectThenDisconnectThenEnterMainMode_throwsException() {
        val scanner = Scanner(mock(), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()
        scanner.disconnect().blockingAwait()
        scanner.enterMainMode().testSubscribe().await().assertError(NotConnectedException::class.java)
    }

    @Test
    fun scanner_connect_callsConnectOnRootMessageStreams() {
        val mockMessageInputStream = mock<RootMessageInputStream>()
        val mockMessageOutputStream = mock<RootMessageOutputStream>()
        val mockInputStream = mock<InputStream>()
        val mockOutputStream = mock<OutputStream>()

        val scanner = Scanner(mock(), RootMessageChannel(mockMessageInputStream, mockMessageOutputStream), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mockInputStream, mockOutputStream).blockingAwait()

        verifyOnce(mockMessageInputStream) { connect(mockInputStream) }
        verifyOnce(mockMessageOutputStream) { connect(mockOutputStream) }
    }

    @Test
    fun scanner_connect_stateIsInRootMode() {
        val scanner = Scanner(mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock())
        scanner.connect(mock(), mock()).blockingAwait()

        assertThat(scanner.state.mode).isEqualTo(Mode.ROOT)
    }

    @Test
    fun scanner_connectThenEnterMainMode_callsConnectOnMainMessageStreams() {

        val mockMessageInputStream = setupMock<MainMessageInputStream> {
            whenThis { veroEvents } thenReturn Flowable.empty()
        }
        val mockMessageOutputStream = mock<MainMessageOutputStream>()
        val mockInputStream = mock<InputStream>()
        val mockOutputStream = mock<OutputStream>()

        val scanner = Scanner(MainMessageChannel(mockMessageInputStream, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mockInputStream, mockOutputStream).blockingAwait()

        scanner.enterMainMode().blockingAwait()

        verifyOnce(mockMessageInputStream) { connect(mockInputStream) }
        verifyOnce(mockMessageOutputStream) { connect(mockOutputStream) }
    }

    @Test
    fun scanner_connectThenEnterMainMode_stateIsInMainMode() {
        val mockMessageInputStream = setupMock<MainMessageInputStream> {
            whenThis { veroEvents } thenReturn Flowable.empty()
        }

        val scanner = Scanner(MainMessageChannel(mockMessageInputStream, mock()), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()

        scanner.enterMainMode().blockingAwait()

        assertThat(scanner.state.mode).isEqualTo(Mode.MAIN)
    }

    @Test
    fun scanner_connectThenEnterCypressOtaMode_callsConnectOnCypressOtaMessageStreams() {

        val mockMessageInputStream = setupMock<CypressOtaMessageInputStream> {
            whenThis { cypressOtaResponseStream } thenReturn Flowable.empty()
        }
        val mockMessageOutputStream = mock<CypressOtaMessageOutputStream>()
        val mockInputStream = mock<InputStream>()
        val mockOutputStream = mock<OutputStream>()

        val scanner = Scanner(mock(), setupRootMessageChannelMock(), CypressOtaMessageChannel(mockMessageInputStream, mockMessageOutputStream), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mockInputStream, mockOutputStream).blockingAwait()

        scanner.enterCypressOtaMode().blockingAwait()

        verifyOnce(mockMessageInputStream) { connect(mockInputStream) }
        verifyOnce(mockMessageOutputStream) { connect(mockOutputStream) }
    }

    @Test
    fun scanner_connectThenEnterCypressOtaMode_stateIsInCypressOtaMode() {
        val mockMessageInputStream = setupMock<CypressOtaMessageInputStream> {
            whenThis { cypressOtaResponseStream } thenReturn Flowable.empty()
        }

        val scanner = Scanner(mock(), setupRootMessageChannelMock(), CypressOtaMessageChannel(mockMessageInputStream, mock()), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()

        scanner.enterCypressOtaMode().blockingAwait()

        assertThat(scanner.state.mode).isEqualTo(Mode.CYPRESS_OTA)
    }

    @Test
    fun scanner_connectThenEnterCypressOtaModeThenStartCypressOta_receivesProgressCorrectly() {
        val progressValues = listOf(0.25f, 0.50f, 0.75f, 1.00f)

        val mockCypressOtaController = setupMock<CypressOtaController> {
            whenThis { program(anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn Observable.defer { progressValues.toObservable() }
        }

        val mockMessageInputStream = setupMock<CypressOtaMessageInputStream> {
            whenThis { cypressOtaResponseStream } thenReturn Flowable.empty()
        }

        val scanner = Scanner(mock(), setupRootMessageChannelMock(), CypressOtaMessageChannel(mockMessageInputStream, mock()), mock(), mockCypressOtaController, mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()
        scanner.enterCypressOtaMode().blockingAwait()

        val testObserver = scanner.startCypressOta(byteArrayOf()).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(progressValues).inOrder()
        testObserver.assertComplete()
    }

    @Test
    fun scanner_connectThenEnterStmOtaMode_callsConnectOnStmOtaMessageStreams() {

        val mockMessageInputStream = setupMock<StmOtaMessageInputStream> {
            whenThis { stmOtaResponseStream } thenReturn Flowable.empty()
        }
        val mockMessageOutputStream = mock<StmOtaMessageOutputStream>()
        val mockInputStream = mock<InputStream>()
        val mockOutputStream = mock<OutputStream>()

        val scanner = Scanner(mock(), setupRootMessageChannelMock(), mock(), StmOtaMessageChannel(mockMessageInputStream, mockMessageOutputStream), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mockInputStream, mockOutputStream).blockingAwait()

        scanner.enterStmOtaMode().blockingAwait()

        verifyOnce(mockMessageInputStream) { connect(mockInputStream) }
        verifyOnce(mockMessageOutputStream) { connect(mockOutputStream) }
    }

    @Test
    fun scanner_connectThenEnterStmOtaMode_stateIsInStmOtaMode() {
        val mockMessageInputStream = setupMock<StmOtaMessageInputStream> {
            whenThis { stmOtaResponseStream } thenReturn Flowable.empty()
        }

        val scanner = Scanner(mock(), setupRootMessageChannelMock(), mock(), StmOtaMessageChannel(mockMessageInputStream, mock()), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()

        scanner.enterStmOtaMode().blockingAwait()

        assertThat(scanner.state.mode).isEqualTo(Mode.STM_OTA)
    }

    @Test
    fun scanner_connectThenEnterStmOtaModeThenStartStmOta_receivesProgressCorrectly() {
        val progressValues = listOf(0.25f, 0.50f, 0.75f, 1.00f)

        val mockStmOtaController = setupMock<StmOtaController> {
            whenThis { program(anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn Observable.defer { progressValues.toObservable() }
        }

        val mockMessageInputStream = setupMock<StmOtaMessageInputStream> {
            whenThis { stmOtaResponseStream } thenReturn Flowable.empty()
        }

        val scanner = Scanner(mock(), setupRootMessageChannelMock(), mock(), StmOtaMessageChannel(mockMessageInputStream, mock()), mock(), mockStmOtaController, mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()
        scanner.enterStmOtaMode().blockingAwait()

        val testObserver = scanner.startStmOta(byteArrayOf()).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(progressValues).inOrder()
        testObserver.assertComplete()
    }

    @Test
    fun scanner_connectThenTurnUn20On_throwsException() {
        val mockMessageInputStream = spy(MainMessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { veroResponses } thenReturn Flowable.empty()
            whenThis { veroEvents } thenReturn Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MainMessageOutputStream> {
            whenThis { sendMessage(anyNotNull()) } thenReturn Completable.complete()
        }

        val scanner = Scanner(MainMessageChannel(mockMessageInputStream, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()

        scanner.turnUn20OnAndAwaitStateChangeEvent().testSubscribe().await().assertError(IncorrectModeException::class.java)
    }

    @Test
    fun scannerVeroEvents_differentKindsOfEventsCreated_forwardsOnlyTriggerEventsToObservers() {
        val eventsSubject = PublishSubject.create<VeroEvent>()

        val mockMessageInputStream = setupMock<MainMessageInputStream> {
            whenThis { veroEvents } thenReturn eventsSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        val mockMessageOutputStream = mock<MainMessageOutputStream>()

        val scanner = Scanner(MainMessageChannel(mockMessageInputStream, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()
        scanner.enterMainMode().blockingAwait()

        val testObserver = TestObserver<Unit>()
        scanner.triggerButtonListeners.add(testObserver)

        val numberOfEvents = 3
        repeat(numberOfEvents) { eventsSubject.onNext(Un20StateChangeEvent(DigitalValue.TRUE)) }
        repeat(numberOfEvents) { eventsSubject.onNext(TriggerButtonPressedEvent()) }

        assertThat(testObserver.valueCount()).isEqualTo(numberOfEvents)
    }

    @Test
    fun scanner_turnOnAndOffUn20_changesStateCorrectlyUponStateChangeEvent() {
        val eventsSubject = PublishSubject.create<VeroEvent>()
        val responseSubject = PublishSubject.create<VeroResponse>()

        val messageInputStreamSpy = spy(MainMessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            veroResponses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = eventsSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        val mockMessageOutputStream = setupMock<MainMessageOutputStream> {
            whenThis { sendMessage(isA<SetUn20OnCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(SetUn20OnResponse(OperationResultCode.OK))
                    eventsSubject.onNext(Un20StateChangeEvent((it.arguments[0] as SetUn20OnCommand).value))
                }
            }
        }

        val scanner = Scanner(MainMessageChannel(messageInputStreamSpy, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()
        scanner.enterMainMode().blockingAwait()

        scanner.turnUn20OnAndAwaitStateChangeEvent().testSubscribe().awaitAndAssertSuccess()
        assertThat(scanner.state.un20On).isTrue()

        scanner.turnUn20OffAndAwaitStateChangeEvent().testSubscribe().awaitAndAssertSuccess()
        assertThat(scanner.state.un20On).isFalse()
    }

    @Test
    fun scanner_setSmileLedState_changesStateCorrectly() {
        val responseSubject = PublishSubject.create<VeroResponse>()

        val messageInputStreamSpy = spy(MainMessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            veroResponses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MainMessageOutputStream> {
            whenThis { sendMessage(isA<SetSmileLedStateCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(SetSmileLedStateResponse(OperationResultCode.OK))
                }
            }
        }

        val scanner = Scanner(MainMessageChannel(messageInputStreamSpy, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()
        scanner.enterMainMode().blockingAwait()

        val smileLedState = SmileLedState(
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00, 0x04)
        )

        scanner.setSmileLedState(smileLedState).testSubscribe().awaitAndAssertSuccess()
        assertThat(scanner.state.smileLedState).isEqualTo(smileLedState)
    }

    @Test
    fun scanner_captureFingerprintWithUn20On_receivesFingerprint() {
        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamSpy = spy(MainMessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MainMessageOutputStream> {
            whenThis { sendMessage(isA<CaptureFingerprintCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(CaptureFingerprintResponse(CaptureFingerprintResult.OK))
                }
            }
        }

        val scanner = Scanner(MainMessageChannel(messageInputStreamSpy, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler).apply {
            connect(mock(), mock()).blockingAwait()
            enterMainMode().blockingAwait()
            state.un20On = true
        }

        scanner.captureFingerprint().testSubscribe().awaitAndAssertSuccess()
    }

    @Test
    fun scanner_captureFingerprintWithUn20Off_throwsException() {
        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamSpy = spy(MainMessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MainMessageOutputStream> {
            whenThis { sendMessage(isA<CaptureFingerprintCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(CaptureFingerprintResponse(CaptureFingerprintResult.OK))
                }
            }
        }

        val scanner = Scanner(MainMessageChannel(messageInputStreamSpy, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler).apply {
            connect(mock(), mock()).blockingAwait()
            enterMainMode().blockingAwait()
            state.un20On = null
        }

        scanner.captureFingerprint().testSubscribe().await().assertError(IllegalUn20StateException::class.java)
    }

    @Test
    fun scanner_acquireTemplateWithUn20On_receivesTemplate() {
        val template = byteArrayOf(0x10, 0x20, 0x30, 0x40)
        val expectedResponseData = byteArrayOf(template)

        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamSpy = spy(MainMessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MainMessageOutputStream> {
            whenThis { sendMessage(isA<GetTemplateCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(GetTemplateResponse(TemplateData(Scanner.DEFAULT_TEMPLATE_TYPE, template)))
                }
            }
        }

        val scanner = Scanner(MainMessageChannel(messageInputStreamSpy, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler).apply {
            connect(mock(), mock()).blockingAwait()
            enterMainMode().blockingAwait()
            state.un20On = true
        }

        val testObserver = scanner.acquireTemplate().testSubscribe()
        testObserver.awaitAndAssertSuccess()
        testObserver.assertValueCount(1)
        testObserver.values().first().let {
            assertThat(byteArrayOf(it.template)).isEqualTo(expectedResponseData)
        }

    }

    @Test
    fun scanner_acquireImageWithUn20On_receivesImage() {
        val image = byteArrayOf(0x10, 0x20, 0x30, 0x40)
        val crcCheck = -42

        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamSpy = spy(MainMessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MainMessageOutputStream> {
            whenThis { sendMessage(isA<GetImageCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(GetImageResponse(ImageData(ImageFormat.RAW, image, crcCheck)))
                }
            }
        }

        val scanner = Scanner(MainMessageChannel(messageInputStreamSpy, mockMessageOutputStream), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler).apply {
            connect(mock(), mock()).blockingAwait()
            enterMainMode().blockingAwait()
            state.un20On = true
        }

        val testObserver = scanner.acquireImage().testSubscribe()
        testObserver.awaitAndAssertSuccess()
        testObserver.assertValueCount(1)
        assertThat(testObserver.values().first().image).isEqualTo(image)
    }

    @Test
    fun scanner_connectThenDisconnect_resetsToDisconnectedState() {
        val scanner = Scanner(mock(), setupRootMessageChannelMock(), mock(), mock(), mock(), mock(), mock(), responseErrorHandler)
        scanner.connect(mock(), mock()).blockingAwait()
        scanner.disconnect().blockingAwait()

        assertThat(scanner.state).isEqualTo(disconnectedScannerState())
    }

    private fun setupRootMessageChannelMock(): RootMessageChannel {

        val responseSubject = PublishSubject.create<RootResponse>()

        val spyRootMessageInputStream = spy(RootMessageInputStream(mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            whenThis { disconnect() } thenDoNothing {}
            rootResponseStream = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        val mockRootMessageOutputStream = setupMock<RootMessageOutputStream> {
            whenThis { sendMessage(anyNotNull()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(
                        when (it.arguments[0] as RootCommand) {
                            is EnterMainModeCommand -> EnterMainModeResponse()
                            is EnterStmOtaModeCommand -> EnterStmOtaModeResponse()
                            is EnterCypressOtaModeCommand -> EnterCypressOtaModeResponse()
                            else -> throw IllegalArgumentException()
                        }
                    )
                }
            }
        }

        return RootMessageChannel(spyRootMessageInputStream, mockRootMessageOutputStream)
    }
}

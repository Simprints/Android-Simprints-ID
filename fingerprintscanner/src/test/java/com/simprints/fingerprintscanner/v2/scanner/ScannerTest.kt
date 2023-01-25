package com.simprints.fingerprintscanner.v2.scanner

import com.google.common.truth.Truth.assertThat
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
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.*
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses.SetSmileLedStateResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterCypressOtaModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterStmOtaModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterCypressOtaModeResponse
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterStmOtaModeResponse
import com.simprints.fingerprintscanner.v2.domain.root.responses.GetExtendedVersionResponse
import com.simprints.fingerprintscanner.v2.exceptions.state.IllegalUn20StateException
import com.simprints.fingerprintscanner.v2.exceptions.state.IncorrectModeException
import com.simprints.fingerprintscanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
import com.simprints.fingerprintscanner.v2.scanner.ota.cypress.CypressOtaController
import com.simprints.fingerprintscanner.v2.scanner.ota.stm.StmOtaController
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.unit.reactive.testSubscribe
import io.mockk.*
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.observers.TestObserver
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

class ScannerTest {

    private lateinit var eventsSubject: PublishSubject<VeroEvent>
    private lateinit var mockkMessageOutputStream: MainMessageOutputStream
    private lateinit var mockkMessageInputStream: MainMessageInputStream
    private val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.NONE)
    private lateinit var scanner: Scanner
    private lateinit var flowableDisposable: Disposable
    private lateinit var flowable: Flowable<ByteArray>
    private lateinit var mockkInputStream: InputStream

    @Before
    fun setup() {
        eventsSubject = PublishSubject.create<VeroEvent>()

        mockkMessageInputStream = mockk {
            every { connect(any()) } just Runs
            every { disconnect() } just Runs
            every { veroEvents } returns eventsSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        mockkMessageOutputStream = mockk {
            every { connect(any()) } just Runs
            every { disconnect() } just Runs
        }

        flowableDisposable = mockk(relaxed = true)
        flowable = mockk {
            every { subscribeOn(any()) } returns this
            every { publish() } returns mockk {
                every { connect() } returns flowableDisposable
            }
        }
        mockkStatic("com.simprints.fingerprintscanner.v2.tools.reactive.RxInputStreamKt")
        mockkInputStream = mockk {
            every { toFlowable() } returns flowable
        }
        scanner = Scanner(
            MainMessageChannel(mockkMessageInputStream, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
    }

    @Test
    fun scanner_callEnterModeBeforeConnect_throwsException() {
        scanner.enterMainMode().testSubscribe().await()
            .assertError(NotConnectedException::class.java)
    }

    @Test
    fun scanner_connectThenDisconnectThenEnterMainMode_throwsException() {
        scanner.connect(mockk(), mockk()).blockingAwait()
        scanner.disconnect().blockingAwait()
        scanner.enterMainMode().testSubscribe().await()
            .assertError(NotConnectedException::class.java)
    }

    @Test
    fun scanner_connect_callsConnectOnRootMessageStreams() {
        val rootMessageChannel:RootMessageChannel =mockk{
            every { connect(any(),any()) } just runs
        }
        scanner = Scanner(
            MainMessageChannel(mockkMessageInputStream, mockkMessageOutputStream),
            rootMessageChannel,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()

        verify { rootMessageChannel.connect(any(),any()) }
    }

    @Test
    fun scanner_connect_stateIsInRootMode() {
        scanner.connect(mockkInputStream, mockk()).blockingAwait()
        assertThat(scanner.state.mode).isEqualTo(Mode.ROOT)
    }

    @Test
    fun scanner_connectThenEnterMainMode_callsConnectOnMainMessageStreams() {
        val mockkOutputStream = mockk<OutputStream>()
        scanner.connect(mockkInputStream, mockkOutputStream).blockingAwait()

        scanner.enterMainMode().blockingAwait()
        verify { mockkMessageInputStream.connect(any()) }
        verify { mockkMessageOutputStream.connect(mockkOutputStream) }

        scanner.disconnect().blockingAwait()
        verify { mockkMessageInputStream.disconnect() }
        verify { mockkMessageOutputStream.disconnect() }

    }

    @Test
    fun scanner_connectThenEnterMainMode_stateIsInMainMode() {

        scanner.connect(mockkInputStream, mockk()).blockingAwait()

        scanner.enterMainMode().blockingAwait()

        assertThat(scanner.state.mode).isEqualTo(Mode.MAIN)
    }
    @Test
    fun `scanner disconnect does nothing when scanner already disconnected`() {
        val mockCypressOtaMessageChannel = mockk<CypressOtaMessageChannel> {
            every { disconnect() } just Runs
        }
        scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockCypressOtaMessageChannel,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.disconnect().blockingAwait()
        verify(exactly = 0) { mockCypressOtaMessageChannel.disconnect() }
    }

    @Test
    fun scanner_connectThenEnterCypressOtaMode_callsConnectOnCypressOtaMessageStreams() {

        val mockCypressOtaMessageChannel = mockk<CypressOtaMessageChannel> {
            every { connect(any(), any()) } just Runs
            every { disconnect() } just Runs
        }
        val mockkOutputStream = mockk<OutputStream>()

        scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockCypressOtaMessageChannel,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockkOutputStream).blockingAwait()

        scanner.enterCypressOtaMode().blockingAwait()

        verify { mockCypressOtaMessageChannel.connect(any(), any()) }
        scanner.disconnect().blockingAwait()
        verify { mockCypressOtaMessageChannel.disconnect() }
    }

    @Test
    fun scanner_connectThenEnterCypressOtaMode_stateIsInCypressOtaMode() {

        val mockkCypressOtaMessageChannel: CypressOtaMessageChannel = mockk {
            every { connect(any(), any()) } just Runs
        }
        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockkCypressOtaMessageChannel,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()

        scanner.enterCypressOtaMode().blockingAwait()

        assertThat(scanner.state.mode).isEqualTo(Mode.CYPRESS_OTA)
    }

    @Test
    fun scanner_connectThenEnterCypressOtaModeThenStartCypressOta_receivesProgressCorrectly() {
        val progressValues = listOf(0.25f, 0.50f, 0.75f, 1.00f)

        val mockkCypressOtaController = mockk<CypressOtaController> {
            every {
                program(
                    any(),
                    any(),
                    any()
                )
            } returns Observable.defer { progressValues.toObservable() }
        }

        val mockkMessageInputStream = mockk<CypressOtaMessageInputStream>(relaxed = true) {
            every { cypressOtaResponseStream } returns Flowable.empty()
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            CypressOtaMessageChannel(mockkMessageInputStream, mockk(relaxed = true)),
            mockk(),
            mockkCypressOtaController,
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()
        scanner.enterCypressOtaMode().blockingAwait()

        val testObserver = scanner.startCypressOta(byteArrayOf()).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(progressValues).inOrder()
        testObserver.assertComplete()
    }

    @Test
    fun scanner_connectThenEnterStmOtaMode_callsConnectOnStmOtaMessageStreams() {

        val mockkMessageInputStream = mockk<StmOtaMessageInputStream> {
            every { connect(any()) } just Runs
            every { disconnect() } just Runs
            every { stmOtaResponseStream } returns Flowable.empty()
        }
        val mockkMessageOutputStream = mockk<StmOtaMessageOutputStream> {
            every { connect(any()) } just Runs
            every { disconnect() } just Runs
        }

        val mockkOutputStream = mockk<OutputStream>()

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            StmOtaMessageChannel(mockkMessageInputStream, mockkMessageOutputStream),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockkOutputStream).blockingAwait()

        scanner.enterStmOtaMode().blockingAwait()

        verify { mockkMessageInputStream.connect(any()) }
        verify { mockkMessageOutputStream.connect(mockkOutputStream) }
        scanner.disconnect().blockingAwait()
        verify { mockkMessageInputStream.disconnect() }
        verify { mockkMessageOutputStream.disconnect() }
    }

    @Test
    fun scanner_connectThenEnterStmOtaMode_stateIsInStmOtaMode() {
        val mockkMessageInputStream = mockk<StmOtaMessageInputStream> {
            every { connect(any()) } just Runs
            every { stmOtaResponseStream } returns Flowable.empty()
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            StmOtaMessageChannel(mockkMessageInputStream, mockk(relaxed = true)),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()

        scanner.enterStmOtaMode().blockingAwait()

        assertThat(scanner.state.mode).isEqualTo(Mode.STM_OTA)
    }

    @Test
    fun scanner_connectThenEnterStmOtaModeThenStartStmOta_receivesProgressCorrectly() {
        val progressValues = listOf(0.25f, 0.50f, 0.75f, 1.00f)

        val mockkStmOtaController = mockk<StmOtaController> {
            every {
                program(
                    any(),
                    any(),
                    any()
                )
            } returns Observable.defer { progressValues.toObservable() }
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            StmOtaMessageChannel(mockk(relaxed = true), mockk(relaxed = true)),
            mockk(),
            mockkStmOtaController,
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()
        scanner.enterStmOtaMode().blockingAwait()

        val testObserver = scanner.startStmOta(byteArrayOf()).testSubscribe()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(progressValues).inOrder()
        testObserver.assertComplete()
    }

    @Test
    fun scanner_connectThenTurnUn20On_throwsException() {
        val mockkMainMessageChannel = mockk<MainMessageChannel> {
            every {
                sendMainModeCommandAndReceiveResponse<SetUn20OnResponse>(any<SetUn20OnCommand>())
            } returns Single.just(SetUn20OnResponse(OperationResultCode.OK))
        }
        val scanner = Scanner(
            mockkMainMessageChannel,
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()

        scanner.turnUn20OnAndAwaitStateChangeEvent().testSubscribe().await()
            .assertError(IncorrectModeException::class.java)
    }

    @Test
    fun scannerVeroEvents_differentKindsOfEventsCreated_forwardsOnlyTriggerEventsToObservers() {


        scanner.connect(mockkInputStream, mockk()).blockingAwait()
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

        val messageInputStreamMock = mockk<MainMessageInputStream> {
            every { connect(any()) } just Runs
            every { veroResponses } returns responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            every { veroEvents } returns eventsSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        val mockkMessageOutputStream = mockk<MainMessageOutputStream>(relaxed = true) {
            every { sendMessage(any<SetUn20OnCommand>()) } answers {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(SetUn20OnResponse(OperationResultCode.OK))
                    eventsSubject.onNext(Un20StateChangeEvent((args[0] as SetUn20OnCommand).value))
                }
            }
        }

        val scanner = Scanner(
            MainMessageChannel(messageInputStreamMock, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()
        scanner.enterMainMode().blockingAwait()

        scanner.turnUn20OnAndAwaitStateChangeEvent().testSubscribe().awaitAndAssertSuccess()
        assertThat(scanner.state.un20On).isTrue()

        scanner.turnUn20OffAndAwaitStateChangeEvent().testSubscribe().awaitAndAssertSuccess()
        assertThat(scanner.state.un20On).isFalse()
    }

    @Test
    fun scanner_setSmileLedState_changesStateCorrectly() {
        val responseSubject = PublishSubject.create<VeroResponse>()

        val messageInputStreamspyk =
            spyk(MainMessageInputStream(mockk(), mockk(), mockk(), mockk())).apply {
                every { connect(any()) } just Runs
                veroResponses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
                veroEvents = Flowable.empty()
            }
        val mockkMessageOutputStream = mockk<MainMessageOutputStream> {
            every { connect(any()) } just Runs
            every { sendMessage(any<SetSmileLedStateCommand>()) } answers {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(SetSmileLedStateResponse(OperationResultCode.OK))
                }
            }
        }

        val scanner = Scanner(
            MainMessageChannel(messageInputStreamspyk, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()
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

        val messageInputStreamspyk =
            spyk(MainMessageInputStream(mockk(), mockk(), mockk(), mockk())).apply {
                every { connect(any()) } just Runs
                un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
                veroEvents = Flowable.empty()
            }
        val mockkMessageOutputStream = mockk<MainMessageOutputStream> {
            every { connect(any()) } just Runs
            every { sendMessage(any<CaptureFingerprintCommand>()) } answers {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(CaptureFingerprintResponse(CaptureFingerprintResult.OK))
                }
            }
        }

        val scanner = Scanner(
            MainMessageChannel(messageInputStreamspyk, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        ).apply {
            connect(mockk(), mockk()).blockingAwait()
            enterMainMode().blockingAwait()
            state.un20On = true
        }

        scanner.captureFingerprint().testSubscribe().awaitAndAssertSuccess()
    }

    @Test
    fun scanner_captureFingerprintWithUn20Off_throwsException() {
        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamspyk = mockk<MainMessageInputStream> {
            every { connect(any()) } just Runs
            every { un20Responses } returns responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            every { veroEvents } returns Flowable.empty()
        }
        val mockkMessageOutputStream = mockk<MainMessageOutputStream>(relaxed = true) {
            every { sendMessage(any<CaptureFingerprintCommand>()) } answers {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(CaptureFingerprintResponse(CaptureFingerprintResult.OK))
                }
            }
        }

        val scanner = Scanner(
            MainMessageChannel(messageInputStreamspyk, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        ).apply {
            connect(mockk(), mockk()).blockingAwait()
            enterMainMode().blockingAwait()
            state.un20On = null
        }

        scanner.captureFingerprint().testSubscribe().await()
            .assertError(IllegalUn20StateException::class.java)
    }

    @Test
    fun scanner_acquireTemplateWithUn20On_receivesTemplate() {
        val template = byteArrayOf(0x10, 0x20, 0x30, 0x40)
        val expectedResponseData = byteArrayOf(template)

        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamspyk =
            spyk(MainMessageInputStream(mockk(), mockk(), mockk(), mockk())).apply {
                every { connect(any()) } just Runs
                un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
                veroEvents = Flowable.empty()
            }
        val mockkMessageOutputStream = mockk<MainMessageOutputStream>(relaxed = true) {
            every { sendMessage(any<GetTemplateCommand>()) } answers {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(
                        GetTemplateResponse(
                            Scanner.DEFAULT_TEMPLATE_TYPE,
                            TemplateData(template)
                        )
                    )
                }
            }
        }

        val scanner = Scanner(
            MainMessageChannel(messageInputStreamspyk, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        ).apply {
            connect(mockk(), mockk()).blockingAwait()
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

        val messageInputStreamspyk =
            spyk(MainMessageInputStream(mockk(), mockk(), mockk(), mockk())).apply {
                every { connect(any()) } just Runs
                un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
                veroEvents = Flowable.empty()
            }
        val mockkMessageOutputStream = mockk<MainMessageOutputStream>(relaxed = true) {
            every { sendMessage(any<GetImageCommand>()) } answers {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(
                        GetImageResponse(
                            ImageFormat.RAW,
                            ImageData(image, crcCheck)
                        )
                    )
                }
            }
        }

        val scanner = Scanner(
            MainMessageChannel(messageInputStreamspyk, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        ).apply {
            connect(mockk(), mockk()).blockingAwait()
            enterMainMode().blockingAwait()
            state.un20On = true
        }

        val testObserver = scanner.acquireImage().testSubscribe()
        testObserver.awaitAndAssertSuccess()
        testObserver.assertValueCount(1)
        assertThat(testObserver.values().first().image).isEqualTo(image)
    }

    @Test
    fun scanner_acquireImage_noImageTakenCallsComplete() {
        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamspyk =
            spyk(MainMessageInputStream(mockk(), mockk(), mockk(), mockk())).apply {
                every { connect(any()) } just Runs
                un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
                veroEvents = Flowable.empty()
            }
        val mockkMessageOutputStream = mockk<MainMessageOutputStream> {
            every { connect(any()) } just Runs
            every { sendMessage(any<GetImageCommand>()) } answers {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(GetImageResponse(ImageFormat.RAW, null))
                }
            }
        }

        val scanner = Scanner(
            MainMessageChannel(messageInputStreamspyk, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            responseErrorHandler
        ).apply {
            connect(mockkInputStream, mockk()).blockingAwait()
            enterMainMode().blockingAwait()
            state.un20On = true
        }

        val testObserver = scanner.acquireImage().testSubscribe()
        testObserver.awaitAndAssertSuccess()
        testObserver.assertValueCount(0)
        testObserver.assertComplete()
    }

    @Test
    fun scanner_connectThenDisconnect_resetsToDisconnectedState() {
        scanner.connect(mockkInputStream, mockk()).blockingAwait()
        scanner.disconnect().blockingAwait()

        assertThat(scanner.state).isEqualTo(disconnectedScannerState())
    }

    @Test
    fun `test scanner disconnect disposes flowableInputStream`() {
        //Given
        val scanner = Scanner(
            mockk(),
            mockk(relaxed = true),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk()
        )

        //When
        scanner.connect(mockkInputStream, mockk(relaxed = true)).blockingAwait()
        scanner.disconnect().blockingAwait()
        //Then
        verify { flowableDisposable.dispose() }
    }

    @Test
    fun scanner_getStmVersion_shouldReturnStmExtendedVersion() {
        val expectedVersion = StmExtendedFirmwareVersion("1.E-1.1")
        val scannerInfoReadermockk = mockk<ScannerExtendedInfoReaderHelper> {
            every { getStmExtendedFirmwareVersion() } returns Single.just(expectedVersion)
        }


        val scanner = Scanner(
            MainMessageChannel(mockkMessageInputStream, mockkMessageOutputStream),
            setupRootMessageChannelMock(),
            scannerInfoReadermockk,
            mockk(), mockk(), mockk(), mockk(), mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()
        scanner.enterMainMode().blockingAwait()

        val testObserver = scanner.getStmFirmwareVersion().test()
        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values().first()).isEqualTo(expectedVersion)
    }


    @Test
    fun scanner_getCypressExtendedVersion_shouldReturnCypressExtendedVersion() {
        val expectedVersion = CypressExtendedFirmwareVersion("1.E-1.1")
        val scannerInfoReadermockk = mockk<ScannerExtendedInfoReaderHelper> {
            every { getCypressExtendedVersion() } returns Single.just(expectedVersion)
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            scannerInfoReadermockk,
            mockk(), mockk(), mockk(), mockk(), mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()

        val testObserver = scanner.getCypressExtendedFirmwareVersion().test()
        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values().first()).isEqualTo(expectedVersion)
    }

    @Test
    fun scanner_getExtendedVersion_shouldReturn_ExtendedVersionInformation() = runBlocking {
        val expectedVersion = mockk<ExtendedVersionInformation>()
        val scannerInfoReadermockk = mockk<ScannerExtendedInfoReaderHelper> {
            coEvery { getExtendedVersionInfo() } returns GetExtendedVersionResponse(expectedVersion)
        }

        val scanner = Scanner(
            mockk(),
            setupRootMessageChannelMock(),
            scannerInfoReadermockk,
            mockk(), mockk(), mockk(), mockk(), mockk(),
            responseErrorHandler
        )
        scanner.connect(mockkInputStream, mockk()).blockingAwait()

        val testObserver = scanner.getExtendedVersionInformation().test()
        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values().first()).isEqualTo(expectedVersion)
    }

    private fun setupRootMessageChannelMock(): RootMessageChannel {

        val responseSubject = PublishSubject.create<RootResponse>()
        val mockRootMessageInputStream = mockk<RootMessageInputStream> {
            every { connect(any()) } just Runs
            every { disconnect() } just Runs
            every { rootResponseStream } returns responseSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        val mockRootMessageOutputStream = mockk<RootMessageOutputStream> {
            every { connect(any()) } just Runs
            every { disconnect() } just Runs
            every { sendMessage(any()) } answers {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(
                        when (args[0] as RootCommand) {
                            is EnterMainModeCommand -> EnterMainModeResponse()
                            is EnterStmOtaModeCommand -> EnterStmOtaModeResponse()
                            is EnterCypressOtaModeCommand -> EnterCypressOtaModeResponse()
                            else -> throw IllegalArgumentException()
                        }
                    )
                }
            }

        }
        return RootMessageChannel(mockRootMessageInputStream, mockRootMessageOutputStream)
    }
}

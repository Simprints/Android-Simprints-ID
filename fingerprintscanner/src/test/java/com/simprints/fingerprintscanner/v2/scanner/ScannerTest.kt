package com.simprints.fingerprintscanner.v2.scanner

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.isA
import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.CaptureFingerprintCommand
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.GetImageCommand
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.GetTemplateCommand
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.TemplateData
import com.simprints.fingerprintscanner.v2.domain.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprintscanner.v2.domain.message.un20.responses.GetImageResponse
import com.simprints.fingerprintscanner.v2.domain.message.un20.responses.GetTemplateResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.SetSmileLedStateCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.*
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.SetSmileLedStateResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprintscanner.v2.incoming.MessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.MessageOutputStream
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

class ScannerTest {

    @Test
    fun scannerConnect_callsConnectOnMessageStreams() {
        val mockMessageInputStream = setupMock<MessageInputStream> {
            whenThis { veroEvents } thenReturn Flowable.empty()
        }
        val mockMessageOutputStream = mock<MessageOutputStream>()
        val mockInputStream = mock<InputStream>()
        val mockOutputStream = mock<OutputStream>()

        val scanner = Scanner(mockMessageInputStream, mockMessageOutputStream)
        scanner.connect(mockInputStream, mockOutputStream)

        verifyOnce(mockMessageInputStream) { connect(mockInputStream) }
        verifyOnce(mockMessageOutputStream) { connect(mockOutputStream) }
    }

    @Test
    fun scannerVeroEvents_differentKindsOfEventsCreated_forwardsOnlyTriggerEventsToObservers() {
        val eventsSubject = PublishSubject.create<VeroEvent>()

        val mockMessageInputStream = setupMock<MessageInputStream> {
            whenThis { veroEvents } thenReturn eventsSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        val mockMessageOutputStream = mock<MessageOutputStream>()

        val scanner = Scanner(mockMessageInputStream, mockMessageOutputStream)
        scanner.connect(mock(), mock())

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

        val messageInputStreamSpy = spy(MessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            veroResponses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = eventsSubject.toFlowable(BackpressureStrategy.BUFFER)
        }
        val mockMessageOutputStream = setupMock<MessageOutputStream> {
            whenThis { sendMessage(isA<SetUn20OnCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(SetUn20OnResponse(OperationResultCode.OK))
                    eventsSubject.onNext(Un20StateChangeEvent((it.arguments[0] as SetUn20OnCommand).value))
                }
            }
        }

        val scanner = Scanner(messageInputStreamSpy, mockMessageOutputStream)
        scanner.connect(mock(), mock())

        scanner.turnUn20OnAndAwaitStateChangeEvent().testSubscribe().awaitAndAssertSuccess()
        assertThat(scanner.state.un20On).isTrue()

        scanner.turnUn20OffAndAwaitStateChangeEvent().testSubscribe().awaitAndAssertSuccess()
        assertThat(scanner.state.un20On).isFalse()
    }

    @Test
    fun scanner_setSmileLedState_changesStateCorrectly() {
        val responseSubject = PublishSubject.create<VeroResponse>()

        val messageInputStreamSpy = spy(MessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            veroResponses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MessageOutputStream> {
            whenThis { sendMessage(isA<SetSmileLedStateCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(SetSmileLedStateResponse(OperationResultCode.OK))
                }
            }
        }

        val scanner = Scanner(messageInputStreamSpy, mockMessageOutputStream)
        scanner.connect(mock(), mock())

        val smileLedState = SmileLedState(
            LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04),
            LedState(DigitalValue.FALSE, 0x00, 0x00 ,0x04)
        )

        scanner.setSmileLedState(smileLedState).testSubscribe().awaitAndAssertSuccess()
        assertThat(scanner.state.smileLedState).isEqualTo(smileLedState)
    }

    @Test
    fun scanner_captureFingerprintWithUn20On_receivesFingerprint() {
        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamSpy = spy(MessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MessageOutputStream> {
            whenThis { sendMessage(isA<CaptureFingerprintCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(CaptureFingerprintResponse(CaptureFingerprintResponse.ResponseCode.OK))
                }
            }
        }

        val scanner = Scanner(messageInputStreamSpy, mockMessageOutputStream).apply {
            connect(mock(), mock())
            state.un20On = true
        }

        scanner.captureFingerprint().testSubscribe().awaitAndAssertSuccess()
    }

    @Test
    fun scanner_captureFingerprintWithUn20Off_throwsException() {
        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamSpy = spy(MessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MessageOutputStream> {
            whenThis { sendMessage(isA<CaptureFingerprintCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(CaptureFingerprintResponse(CaptureFingerprintResponse.ResponseCode.OK))
                }
            }
        }

        val scanner = Scanner(messageInputStreamSpy, mockMessageOutputStream).apply {
            connect(mock(), mock())
            state.un20On = null
        }

        scanner.captureFingerprint().testSubscribe().await().assertError(NotImplementedError::class.java) // TODO : Exception handling
    }

    @Test
    fun scanner_acquireTemplateWithUn20On_receivesTemplate() {
        val templateQuality = 80
        val template = byteArrayOf(0x10, 0x20, 0x30, 0x40)
        val expectedResponseData = byteArrayOf(templateQuality, template)

        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamSpy = spy(MessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MessageOutputStream> {
            whenThis { sendMessage(isA<GetTemplateCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(GetTemplateResponse(TemplateData(Scanner.DEFAULT_TEMPLATE_TYPE, templateQuality, template)))
                }
            }
        }

        val scanner = Scanner(messageInputStreamSpy, mockMessageOutputStream).apply {
            connect(mock(), mock())
            state.un20On = true
        }

        val testObserver = scanner.acquireTemplate().testSubscribe()
        testObserver.awaitAndAssertSuccess()
        testObserver.assertValueCount(1)
        testObserver.values().first().let {
            assertThat(byteArrayOf(it.quality, it.template)).isEqualTo(expectedResponseData)
        }

    }

    @Test
    fun scanner_acquireImageWithUn20On_receivesImage() {
        val image = byteArrayOf(0x10, 0x20, 0x30, 0x40)

        val responseSubject = PublishSubject.create<Un20Response>()

        val messageInputStreamSpy = spy(MessageInputStream(mock(), mock(), mock(), mock())).apply {
            whenThis { connect(anyNotNull()) } thenDoNothing {}
            un20Responses = responseSubject.toFlowable(BackpressureStrategy.BUFFER)
            veroEvents = Flowable.empty()
        }
        val mockMessageOutputStream = setupMock<MessageOutputStream> {
            whenThis { sendMessage(isA<GetImageCommand>()) } then {
                Completable.complete().doAfterTerminate {
                    responseSubject.onNext(GetImageResponse(Scanner.DEFAULT_IMAGE_FORMAT, image))
                }
            }
        }

        val scanner = Scanner(messageInputStreamSpy, mockMessageOutputStream).apply {
            connect(mock(), mock())
            state.un20On = true
        }

        val testObserver = scanner.acquireImage().testSubscribe()
        testObserver.awaitAndAssertSuccess()
        testObserver.assertValueCount(1)
        assertThat(testObserver.values().first()).isEqualTo(image)
    }
}

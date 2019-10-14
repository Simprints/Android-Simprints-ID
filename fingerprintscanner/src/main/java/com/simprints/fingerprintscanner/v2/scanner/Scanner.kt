package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentSocket
import com.simprints.fingerprintscanner.v2.domain.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.CaptureFingerprintCommand
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.GetImageCommand
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.GetTemplateCommand
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Dpi
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.ImageFormat
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprintscanner.v2.domain.message.un20.responses.GetImageResponse
import com.simprints.fingerprintscanner.v2.domain.message.un20.responses.GetTemplateResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.SetSmileLedStateCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.SetSmileLedStateResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprintscanner.v2.incoming.MessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.MessageOutputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import io.reactivex.Completable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

class Scanner(
    private val messageInputStream: MessageInputStream,
    private val messageOutputStream: MessageOutputStream
) : Connectable {

    val state = ScannerState(
        connected = false,
        firmwareVersion = null,
        un20On = null,
        triggerButtonActive = false
    )

    val triggerButtonListeners = mutableSetOf<Observer<Unit>>()

    private lateinit var socket: BluetoothComponentSocket

    private val disposables = mutableListOf<Disposable>()

    override fun connect(socket: BluetoothComponentSocket) {
        this.socket = socket
        this.socket.connect()
        messageInputStream.connect(socket.getInputStream())
        messageOutputStream.connect(socket.getOutputStream())
        state.connected = true
        state.triggerButtonActive = true
        disposables.add(subscribeTriggerButtonListeners())
    }

    override fun disconnect() {
        messageInputStream.disconnect()
        messageOutputStream.disconnect()
        socket.close()
        state.connected = false
        state.triggerButtonActive = false
        disposables.forEach { it.dispose() }
    }

    private inline fun <reified R : IncomingMessage> sendCommandAndReceiveResponse(command: OutgoingMessage): Single<R> =
        messageOutputStream.sendMessage(command)
            .andThen(messageInputStream.receiveResponse())

    private fun subscribeTriggerButtonListeners() =
        messageInputStream.veroEvents
            .filterCast<TriggerButtonPressedEvent>()
            .subscribeBy(onNext = {
                triggerButtonListeners.forEach { it.onNext(Unit) }
            })

    fun setSmileLedState(smileLedState: SmileLedState): Completable =
        sendCommandAndReceiveResponse<SetSmileLedStateResponse>(
            SetSmileLedStateCommand(smileLedState)
        ).ignoreElement()

    fun turnUn20OnAndAwaitStateChangeEvent(): Completable =
        sendCommandAndReceiveResponse<SetUn20OnResponse>(
            SetUn20OnCommand(DigitalValue.TRUE)
        ).ignoreElement()
            .andThen(messageInputStream.veroEvents)
            .filterCast<Un20StateChangeEvent> { it.value == DigitalValue.TRUE }
            .firstOrError()
            .ignoreElement()
            .doOnComplete { state.un20On = true }

    fun turnUn20OffAndAwaitStateChangeEvent(): Completable =
        sendCommandAndReceiveResponse<SetUn20OnResponse>(
            SetUn20OnCommand(DigitalValue.FALSE)
        ).ignoreElement()
            .andThen(messageInputStream.veroEvents)
            .filterCast<Un20StateChangeEvent> { it.value == DigitalValue.FALSE }
            .firstOrError()
            .ignoreElement()
            .doOnComplete { state.un20On = false }

    private fun assertUn20On() = Completable.fromAction {
        if (state.un20On != true) {
            TODO("exception handling")
        }
    }

    fun captureFingerprint(dpi: Dpi = DEFAULT_DPI): Completable =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<CaptureFingerprintResponse>(
                CaptureFingerprintCommand(dpi)
            )
        ).ignoreElement()

    fun acquireTemplate(templateType: TemplateType = DEFAULT_TEMPLATE_TYPE): Single<ByteArray> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<GetTemplateResponse>(
                GetTemplateCommand(templateType)
            )
        ).map { it.template }


    fun acquireImage(imageFormat: ImageFormat = DEFAULT_IMAGE_FORMAT): Single<ByteArray> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<GetImageResponse>(
                GetImageCommand(imageFormat)
            )
        ).map { it.image }


    companion object {
        val DEFAULT_DPI = Dpi(500)
        val DEFAULT_TEMPLATE_TYPE = TemplateType.ISO_19794_2_2011
        val DEFAULT_IMAGE_FORMAT = ImageFormat.RAW
    }
}

package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.domain.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.*
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.*
import com.simprints.fingerprintscanner.v2.domain.message.un20.responses.*
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.*
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.*
import com.simprints.fingerprintscanner.v2.incoming.MessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.MessageOutputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.completeOnceReceived
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import io.reactivex.Completable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import java.io.InputStream
import java.io.OutputStream

class Scanner(
    private val messageInputStream: MessageInputStream,
    private val messageOutputStream: MessageOutputStream
) : Connectable {

    val state = ScannerState(
        connected = false,
        un20On = null,
        triggerButtonActive = false,
        smileLedState = null,
        batteryPercentCharge = null
    )

    val triggerButtonListeners = mutableSetOf<Observer<Unit>>()

    private val disposables = mutableListOf<Disposable>()

    override fun connect(inputStream: InputStream, outputStream: OutputStream) {
        messageInputStream.connect(inputStream)
        messageOutputStream.connect(outputStream)
        state.connected = true
        state.triggerButtonActive = true
        disposables.add(subscribeTriggerButtonListeners())
    }

    override fun disconnect() {
        messageInputStream.disconnect()
        messageOutputStream.disconnect()
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

    fun getStmFirmwareVersion(): Single<StmFirmwareVersion> =
        sendCommandAndReceiveResponse<GetStmFirmwareVersionResponse>(
            GetStmFirmwareVersionCommand()
        ).map { it.stmFirmwareVersion }

    fun getUn20Status(): Single<Boolean> =
        sendCommandAndReceiveResponse<GetUn20OnResponse>(
            GetUn20OnCommand()
        ).map { it.value == DigitalValue.TRUE }
            .doOnSuccess { state.un20On = it }

    fun turnUn20OnAndAwaitStateChangeEvent(): Completable =
        sendCommandAndReceiveResponse<SetUn20OnResponse>(
            SetUn20OnCommand(DigitalValue.TRUE)
        ).completeOnceReceived()
            .andThen(messageInputStream.veroEvents)
            .filterCast<Un20StateChangeEvent> { it.value == DigitalValue.TRUE }
            .completeOnceReceived()
            .doOnComplete { state.un20On = true }

    fun turnUn20OffAndAwaitStateChangeEvent(): Completable =
        sendCommandAndReceiveResponse<SetUn20OnResponse>(
            SetUn20OnCommand(DigitalValue.FALSE)
        ).completeOnceReceived()
            .andThen(messageInputStream.veroEvents)
            .filterCast<Un20StateChangeEvent> { it.value == DigitalValue.FALSE }
            .completeOnceReceived()
            .doOnComplete { state.un20On = false }

    private fun assertUn20On() = Completable.fromAction {
        if (state.un20On != true) {
            TODO("exception handling")
        }
    }

    fun getTriggerButtonStatus(): Single<Boolean> =
        sendCommandAndReceiveResponse<GetTriggerButtonActiveResponse>(
            GetTriggerButtonActiveCommand()
        ).map { it.value == DigitalValue.TRUE }
            .doOnSuccess { state.triggerButtonActive = it }

    fun activateTriggerButton(): Completable =
        sendCommandAndReceiveResponse<SetTriggerButtonActiveResponse>(
            SetTriggerButtonActiveCommand(DigitalValue.TRUE)
        ).completeOnceReceived()
            .doOnComplete { state.triggerButtonActive = true }

    fun deactivateTriggerButton(): Completable =
        sendCommandAndReceiveResponse<SetTriggerButtonActiveResponse>(
            SetTriggerButtonActiveCommand(DigitalValue.FALSE)
        ).completeOnceReceived()
            .doOnComplete { state.triggerButtonActive = false }

    fun getSmileLedState(): Single<SmileLedState> =
        sendCommandAndReceiveResponse<GetSmileLedStateResponse>(
            GetSmileLedStateCommand()
        ).map { it.smileLedState }
            .doOnSuccess { state.smileLedState = it }

    fun getPowerLedState(): Single<LedState> =
        sendCommandAndReceiveResponse<GetPowerLedStateResponse>(
            GetPowerLedStateCommand()
        ).map { it.ledState }

    fun getBluetoothLedState(): Single<LedState> =
        sendCommandAndReceiveResponse<GetBluetoothLedStateResponse>(
            GetBluetoothLedStateCommand()
        ).map { it.ledState }

    fun setSmileLedState(smileLedState: SmileLedState): Completable =
        sendCommandAndReceiveResponse<SetSmileLedStateResponse>(
            SetSmileLedStateCommand(smileLedState)
        ).completeOnceReceived()
            .doOnComplete { state.smileLedState = smileLedState }

    fun getBatteryPercentCharge(): Single<Int> =
        sendCommandAndReceiveResponse<GetBatteryPercentChargeResponse>(
            GetBatteryPercentChargeCommand()
        ).map { it.batteryPercentCharge.percentCharge.toInt() }
            .doOnSuccess { state.batteryPercentCharge = it }

    fun getUn20AppVersion(): Single<Un20AppVersion> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<GetUn20AppVersionResponse>(
                GetUn20AppVersionCommand()
            )
        ).map { it.un20AppVersion }

    fun captureFingerprint(dpi: Dpi = DEFAULT_DPI): Single<CaptureFingerprintResult> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<CaptureFingerprintResponse>(
                CaptureFingerprintCommand(dpi)
            )
        ).map { it.captureFingerprintResult }

    fun getSupportedTemplateTypes(): Single<Set<TemplateType>> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<GetSupportedTemplateTypesResponse>(
                GetSupportedTemplateTypesCommand()
            )
        ).map { it.supportedTemplateTypes }

    fun acquireTemplate(templateType: TemplateType = DEFAULT_TEMPLATE_TYPE): Single<TemplateData> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<GetTemplateResponse>(
                GetTemplateCommand(templateType)
            )
        ).map { it.templateData }

    fun getSupportedImageFormats(): Single<Set<ImageFormat>> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<GetSupportedImageFormatsResponse>(
                GetSupportedImageFormatsCommand()
            )
        ).map { it.supportedImageFormats }

    fun acquireImage(imageFormat: ImageFormat = DEFAULT_IMAGE_FORMAT): Single<ByteArray> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<GetImageResponse>(
                GetImageCommand(imageFormat)
            )
        ).map { it.image }

    fun getImageQualityScore(): Single<Int> =
        assertUn20On().andThen(
            sendCommandAndReceiveResponse<GetImageQualityResponse>(
                GetImageQualityCommand()
            )
        ).map { it.imageQualityScore }

    companion object {
        val DEFAULT_DPI = Dpi(500)
        val DEFAULT_TEMPLATE_TYPE = TemplateType.ISO_19794_2_2011
        val DEFAULT_IMAGE_FORMAT = ImageFormat.RAW
    }
}

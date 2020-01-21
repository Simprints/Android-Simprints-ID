package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.domain.Mode
import com.simprints.fingerprintscanner.v2.domain.Mode.*
import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.*
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.*
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.*
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.*
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses.*
import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprintscanner.v2.stream.MainMessageStream
import com.simprints.fingerprintscanner.v2.stream.RootMessageStream
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
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
    private val mainMessageStream: MainMessageStream,
    private val rootMessageStream: RootMessageStream
) {

    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    val state = ScannerState(
        connected = false,
        mode = null,
        un20On = null,
        triggerButtonActive = null,
        smileLedState = null,
        batteryPercentCharge = null
    )

    val triggerButtonListeners = mutableSetOf<Observer<Unit>>()

    private val disposables = mutableListOf<Disposable>()

    fun connect(inputStream: InputStream, outputStream: OutputStream) {
        this.inputStream = inputStream
        this.outputStream = outputStream
        state.connected = true
        state.mode = ROOT

        rootMessageStream.connect(inputStream, outputStream)
    }

    fun disconnect() {
        when (state.mode) {
            ROOT -> rootMessageStream.disconnect()
            MAIN -> {
                mainMessageStream.disconnect()
                state.triggerButtonActive = false
                disposables.forEach { it.dispose() }
            }
            CYPRESS_OTA -> TODO()
            STM_OTA -> TODO()
            null -> {/* Do nothing */
            }
        }
        state.connected = false
    }

    private fun assertMode(mode: Mode) = Completable.fromAction {
        if (state.mode != mode) {
            TODO("exception handling - Currently in incorrect mode")
        }
    }

    private fun assertUn20On() = Completable.fromAction {
        if (state.un20On != true) {
            TODO("exception handling")
        }
    }

    private inline fun <reified R : RootResponse> sendRootModeCommandAndReceiveResponse(command: RootCommand): Single<R> =
        rootMessageStream.outgoing.sendMessage(command)
            .andThen(rootMessageStream.incoming.receiveResponse())

    fun enterMainMode(): Completable =
        assertMode(ROOT).andThen(
            sendRootModeCommandAndReceiveResponse<EnterMainModeResponse>(
                EnterMainModeCommand()
            ))
            .completeOnceReceived()
            .andThen(handleMainModeEntered())

    private fun handleMainModeEntered() = completable {
        rootMessageStream.disconnect()
        mainMessageStream.connect(inputStream, outputStream)
        state.triggerButtonActive = true
        state.mode = MAIN
        disposables.add(subscribeTriggerButtonListeners())
    }

    private inline fun <reified R : IncomingMessage> sendMainModeCommandAndReceiveResponse(command: OutgoingMessage): Single<R> =
        mainMessageStream.outgoing.sendMessage(command)
            .andThen(mainMessageStream.incoming.receiveResponse())

    private fun subscribeTriggerButtonListeners() =
        mainMessageStream.incoming.veroEvents
            .filterCast<TriggerButtonPressedEvent>()
            .subscribeBy(onNext = {
                triggerButtonListeners.forEach { it.onNext(Unit) }
            })

    fun getStmFirmwareVersion(): Single<StmFirmwareVersion> =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<GetStmFirmwareVersionResponse>(
                GetStmFirmwareVersionCommand()))
            .map { it.stmFirmwareVersion }

    fun getUn20Status(): Single<Boolean> =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<GetUn20OnResponse>(
                GetUn20OnCommand()))
            .map { it.value == DigitalValue.TRUE }
            .doOnSuccess { state.un20On = it }

    fun turnUn20OnAndAwaitStateChangeEvent(): Completable =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<SetUn20OnResponse>(
                SetUn20OnCommand(DigitalValue.TRUE)
            ))
            .completeOnceReceived()
            .andThen(mainMessageStream.incoming.veroEvents)
            .filterCast<Un20StateChangeEvent> { it.value == DigitalValue.TRUE }
            .completeOnceReceived()
            .doOnComplete { state.un20On = true }

    fun turnUn20OffAndAwaitStateChangeEvent(): Completable =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<SetUn20OnResponse>(
                SetUn20OnCommand(DigitalValue.FALSE)
            ))
            .completeOnceReceived()
            .andThen(mainMessageStream.incoming.veroEvents)
            .filterCast<Un20StateChangeEvent> { it.value == DigitalValue.FALSE }
            .completeOnceReceived()
            .doOnComplete { state.un20On = false }

    fun getTriggerButtonStatus(): Single<Boolean> =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<GetTriggerButtonActiveResponse>(
                GetTriggerButtonActiveCommand()
            ))
            .map { it.value == DigitalValue.TRUE }
            .doOnSuccess { state.triggerButtonActive = it }

    fun activateTriggerButton(): Completable =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<SetTriggerButtonActiveResponse>(
                SetTriggerButtonActiveCommand(DigitalValue.TRUE)
            ))
            .completeOnceReceived()
            .doOnComplete { state.triggerButtonActive = true }

    fun deactivateTriggerButton(): Completable =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<SetTriggerButtonActiveResponse>(
                SetTriggerButtonActiveCommand(DigitalValue.FALSE)
            ))
            .completeOnceReceived()
            .doOnComplete { state.triggerButtonActive = false }

    fun getSmileLedState(): Single<SmileLedState> =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<GetSmileLedStateResponse>(
                GetSmileLedStateCommand()
            ))
            .map { it.smileLedState }
            .doOnSuccess { state.smileLedState = it }

    fun getPowerLedState(): Single<LedState> =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<GetPowerLedStateResponse>(
                GetPowerLedStateCommand()
            ))
            .map { it.ledState }

    fun getBluetoothLedState(): Single<LedState> =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<GetBluetoothLedStateResponse>(
                GetBluetoothLedStateCommand()
            ))
            .map { it.ledState }

    fun setSmileLedState(smileLedState: SmileLedState): Completable =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<SetSmileLedStateResponse>(
                SetSmileLedStateCommand(smileLedState)
            ))
            .completeOnceReceived()
            .doOnComplete { state.smileLedState = smileLedState }

    fun getBatteryPercentCharge(): Single<Int> =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<GetBatteryPercentChargeResponse>(
                GetBatteryPercentChargeCommand()
            ))
            .map { it.batteryPercentCharge.percentCharge.toInt() }
            .doOnSuccess { state.batteryPercentCharge = it }

    fun getUn20AppVersion(): Single<Un20AppVersion> =
        assertMode(MAIN).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetUn20AppVersionResponse>(
                GetUn20AppVersionCommand()
            ))
            .map { it.un20AppVersion }

    fun captureFingerprint(dpi: Dpi = DEFAULT_DPI): Single<CaptureFingerprintResult> =
        assertMode(MAIN).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<CaptureFingerprintResponse>(
                CaptureFingerprintCommand(dpi)
            ))
            .map { it.captureFingerprintResult }

    fun getSupportedTemplateTypes(): Single<Set<TemplateType>> =
        assertMode(MAIN).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetSupportedTemplateTypesResponse>(
                GetSupportedTemplateTypesCommand()
            ))
            .map { it.supportedTemplateTypes }

    fun acquireTemplate(templateType: TemplateType = DEFAULT_TEMPLATE_TYPE): Single<TemplateData> =
        assertMode(MAIN).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetTemplateResponse>(
                GetTemplateCommand(templateType)
            ))
            .map { it.templateData }

    fun getSupportedImageFormats(): Single<Set<ImageFormat>> =
        assertMode(MAIN).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetSupportedImageFormatsResponse>(
                GetSupportedImageFormatsCommand()
            ))
            .map { it.supportedImageFormats }

    fun acquireImage(imageFormat: ImageFormat = DEFAULT_IMAGE_FORMAT): Single<ImageData> =
        assertMode(MAIN).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetImageResponse>(
                GetImageCommand(imageFormat)
            ))
            .map { it.imageData }

    fun getImageQualityScore(): Single<Int> =
        assertMode(MAIN).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetImageQualityResponse>(
                GetImageQualityCommand()
            ))
            .map { it.imageQualityScore }

    companion object {
        val DEFAULT_DPI = Dpi(500)
        val DEFAULT_TEMPLATE_TYPE = TemplateType.ISO_19794_2_2011
        val DEFAULT_IMAGE_FORMAT = ImageFormat.RAW
    }
}

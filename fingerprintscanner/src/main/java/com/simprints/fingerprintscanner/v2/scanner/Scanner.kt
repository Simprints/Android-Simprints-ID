package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.domain.Mode
import com.simprints.fingerprintscanner.v2.domain.Mode.*
import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
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
import com.simprints.fingerprintscanner.v2.domain.root.commands.*
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation
import com.simprints.fingerprintscanner.v2.domain.root.responses.*
import com.simprints.fingerprintscanner.v2.ota.stm.StmOtaController
import com.simprints.fingerprintscanner.v2.stream.MainMessageStream
import com.simprints.fingerprintscanner.v2.stream.RootMessageStream
import com.simprints.fingerprintscanner.v2.stream.StmOtaMessageStream
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import com.simprints.fingerprintscanner.v2.tools.reactive.completeOnceReceived
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import java.io.InputStream
import java.io.OutputStream

class Scanner(
    private val mainMessageStream: MainMessageStream,
    private val rootMessageStream: RootMessageStream,
    private val stmOtaMessageStream: StmOtaMessageStream,
    private val stmOtaController: StmOtaController
) {

    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    val state = disconnectedScannerState()

    val triggerButtonListeners = mutableSetOf<Observer<Unit>>()

    private var scannerTriggerListenerDisposable: Disposable? = null

    fun connect(inputStream: InputStream, outputStream: OutputStream): Completable = completable {
        this.inputStream = inputStream
        this.outputStream = outputStream
        state.connected = true
        state.mode = ROOT

        rootMessageStream.connect(inputStream, outputStream)
    }

    fun disconnect(): Completable = completable {
        when (state.mode) {
            ROOT -> rootMessageStream.disconnect()
            MAIN -> {
                mainMessageStream.disconnect()
                scannerTriggerListenerDisposable?.dispose()
            }
            CYPRESS_OTA -> TODO()
            STM_OTA -> stmOtaMessageStream.disconnect()
            null -> {/* Do nothing */
            }
        }
        resetStateToDisconnected()
    }

    private fun resetStateToDisconnected() {
        with(state) {
            disconnectedScannerState().let {
                connected = it.connected
                mode = it.mode
                un20On = it.un20On
                triggerButtonActive = it.triggerButtonActive
                smileLedState = it.smileLedState
                batteryPercentCharge = it.batteryPercentCharge
            }
        }
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
        Single.defer {
            rootMessageStream.outgoing.sendMessage(command)
                .andThen(rootMessageStream.incoming.receiveResponse<R>())
        }

    private inline fun <reified R : IncomingMainMessage> sendMainModeCommandAndReceiveResponse(command: OutgoingMainMessage): Single<R> =
        mainMessageStream.outgoing.sendMessage(command)
            .andThen(mainMessageStream.incoming.receiveResponse<R>())

    fun getVersionInformation(): Single<UnifiedVersionInformation> =
        assertMode(ROOT).andThen(
            sendRootModeCommandAndReceiveResponse<GetVersionResponse>(
                GetVersionCommand()
            ))
            .map { it.version }

    fun setVersionInformation(versionInformation: UnifiedVersionInformation): Completable =
        assertMode(ROOT).andThen(
            sendRootModeCommandAndReceiveResponse<SetVersionResponse>(
                SetVersionCommand(versionInformation)
            ))
            .completeOnceReceived()

    fun enterMainMode(): Completable =
        assertMode(ROOT).andThen(
            sendRootModeCommandAndReceiveResponse<EnterMainModeResponse>(
                EnterMainModeCommand()
            ))
            .completeOnceReceived()
            .andThen(handleMainModeEntered())

    fun enterCypressOtaMode(): Completable =
        assertMode(ROOT).andThen(
            sendRootModeCommandAndReceiveResponse<EnterCypressOtaModeResponse>(
                EnterCypressOtaModeCommand()
            ))
            .completeOnceReceived()
            .doOnComplete { state.mode = CYPRESS_OTA } // TODO : handle Cypress OTA mode entered

    fun enterStmOtaMode(): Completable =
        assertMode(ROOT).andThen(
            sendRootModeCommandAndReceiveResponse<EnterStmOtaModeResponse>(
                EnterStmOtaModeCommand()
            ))
            .completeOnceReceived()
            .andThen(handleStmOtaModeEntered())

    private fun handleMainModeEntered() = completable {
        rootMessageStream.disconnect()
        mainMessageStream.connect(inputStream, outputStream)
        state.triggerButtonActive = true
        state.mode = MAIN
        scannerTriggerListenerDisposable = subscribeTriggerButtonListeners()
    }

    private fun subscribeTriggerButtonListeners() =
        mainMessageStream.incoming.veroEvents
            ?.filterCast<TriggerButtonPressedEvent>()
            ?.subscribeBy(onNext = {
                triggerButtonListeners.forEach { it.onNext(Unit) }
            })

    private fun handleStmOtaModeEntered() = completable {
        rootMessageStream.disconnect()
        stmOtaMessageStream.connect(inputStream, outputStream)
        state.mode = STM_OTA
    }

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
            .andThen(mainMessageStream.incoming.receiveResponse<Un20StateChangeEvent>(
                withPredicate = { it.value == DigitalValue.TRUE })
            )
            .completeOnceReceived()
            .doOnComplete { state.un20On = true }

    fun turnUn20OffAndAwaitStateChangeEvent(): Completable =
        assertMode(MAIN).andThen(
            sendMainModeCommandAndReceiveResponse<SetUn20OnResponse>(
                SetUn20OnCommand(DigitalValue.FALSE)
            ))
            .completeOnceReceived()
            .andThen(mainMessageStream.incoming.receiveResponse<Un20StateChangeEvent>(
                withPredicate = { it.value == DigitalValue.FALSE })
            )
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

    fun startStmOta(firmwareHexFile: String): Observable<Float> =
        assertMode(STM_OTA).andThen(
            stmOtaController.program(stmOtaMessageStream, firmwareHexFile))

    companion object {
        val DEFAULT_DPI = Dpi(500)
        val DEFAULT_TEMPLATE_TYPE = TemplateType.ISO_19794_2_2011
        val DEFAULT_IMAGE_FORMAT = ImageFormat.RAW
    }
}

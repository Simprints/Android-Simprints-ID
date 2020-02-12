package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprintscanner.v2.channel.MainMessageChannel
import com.simprints.fingerprintscanner.v2.channel.RootMessageChannel
import com.simprints.fingerprintscanner.v2.channel.StmOtaMessageChannel
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
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.exceptions.state.IllegalUn20StateException
import com.simprints.fingerprintscanner.v2.exceptions.state.IncorrectModeException
import com.simprints.fingerprintscanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.handleErrorsWith
import com.simprints.fingerprintscanner.v2.scanner.ota.cypress.CypressOtaController
import com.simprints.fingerprintscanner.v2.scanner.ota.stm.StmOtaController
import com.simprints.fingerprintscanner.v2.scanner.ota.un20.Un20OtaController
import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt
import com.simprints.fingerprintscanner.v2.tools.reactive.*
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Methods in this class can throw various subclasses of these exceptions:
 * @throws IOException If disconnection or timeout occurs
 * @throws IllegalStateException On attempting to call certain methods whilst in an incorrect state
 * @throws IllegalArgumentException On receiving unexpected or invalid bytes from the scanner
 */
@Suppress("unused")
class Scanner(
    private val mainMessageChannel: MainMessageChannel,
    private val rootMessageChannel: RootMessageChannel,
    private val cypressOtaMessageChannel: CypressOtaMessageChannel,
    private val stmOtaMessageChannel: StmOtaMessageChannel,
    private val cypressOtaController: CypressOtaController,
    private val stmOtaController: StmOtaController,
    private val un20OtaController: Un20OtaController,
    private val responseErrorHandler: ResponseErrorHandler
) {

    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    var state = disconnectedScannerState()

    val triggerButtonListeners = mutableSetOf<Observer<Unit>>()

    private var scannerTriggerListenerDisposable: Disposable? = null

    fun connect(inputStream: InputStream, outputStream: OutputStream): Completable = completable {
        this.inputStream = inputStream
        this.outputStream = outputStream
        state.connected = true
        state.mode = ROOT

        rootMessageChannel.connect(inputStream, outputStream)
    }

    fun disconnect(): Completable = completable {
        when (state.mode) {
            ROOT -> rootMessageChannel.disconnect()
            MAIN -> {
                mainMessageChannel.disconnect()
                scannerTriggerListenerDisposable?.dispose()
            }
            CYPRESS_OTA -> cypressOtaMessageChannel.disconnect()
            STM_OTA -> stmOtaMessageChannel.disconnect()
            null -> {/* Do nothing */
            }
        }
        state = disconnectedScannerState()
    }

    private fun assertConnected() = Completable.fromAction {
        if (state.connected != true) {
            throw NotConnectedException("Attempting to access functionality before calling Scanner::connect()")
        }
    }

    private fun assertMode(mode: Mode) = Completable.fromAction {
        if (state.mode != mode) {
            throw IncorrectModeException("Attempting to access $mode functionality when current mode is ${state.mode}")
        }
    }

    private fun assertUn20On() = Completable.fromAction {
        if (state.un20On != true) {
            throw IllegalUn20StateException("Attempting to access UN20 functionality when UN20 is off")
        }
    }

    private inline fun <reified R : RootResponse> sendRootModeCommandAndReceiveResponse(command: RootCommand): Single<R> =
        rootMessageChannel.sendRootModeCommandAndReceiveResponse<R>(command)
            .handleErrorsWith(responseErrorHandler)

    private inline fun <reified R : IncomingMainMessage> sendMainModeCommandAndReceiveResponse(command: OutgoingMainMessage): Single<R> =
        mainMessageChannel.sendMainModeCommandAndReceiveResponse<R>(command)
            .handleErrorsWith(responseErrorHandler)

    fun getVersionInformation(): Single<UnifiedVersionInformation> =
        assertConnected().andThen(assertMode(ROOT)).andThen(
            sendRootModeCommandAndReceiveResponse<GetVersionResponse>(
                GetVersionCommand()
            ))
            .map { it.version }

    fun setVersionInformation(versionInformation: UnifiedVersionInformation): Completable =
        assertConnected().andThen(assertMode(ROOT)).andThen(
            sendRootModeCommandAndReceiveResponse<SetVersionResponse>(
                SetVersionCommand(versionInformation)
            ))
            .completeOnceReceived()

    fun enterMainMode(): Completable =
        assertConnected().andThen(assertMode(ROOT)).andThen(
            sendRootModeCommandAndReceiveResponse<EnterMainModeResponse>(
                EnterMainModeCommand()
            ))
            .completeOnceReceived()
            .andThen(handleMainModeEntered())

    fun enterCypressOtaMode(): Completable =
        assertConnected().andThen(assertMode(ROOT)).andThen(
            sendRootModeCommandAndReceiveResponse<EnterCypressOtaModeResponse>(
                EnterCypressOtaModeCommand()
            ))
            .completeOnceReceived()
            .andThen(handleCypressOtaModeEntered())

    fun enterStmOtaMode(): Completable =
        assertConnected().andThen(assertMode(ROOT)).andThen(
            sendRootModeCommandAndReceiveResponse<EnterStmOtaModeResponse>(
                EnterStmOtaModeCommand()
            ))
            .completeOnceReceived()
            .andThen(handleStmOtaModeEntered())

    private fun handleMainModeEntered() = completable {
        rootMessageChannel.disconnect()
        mainMessageChannel.connect(inputStream, outputStream)
        state.triggerButtonActive = true
        state.mode = MAIN
        scannerTriggerListenerDisposable = subscribeTriggerButtonListeners()
    }

    private fun subscribeTriggerButtonListeners() =
        mainMessageChannel.incoming.veroEvents
            ?.filterCast<TriggerButtonPressedEvent>()
            ?.subscribeBy(onNext = {
                triggerButtonListeners.forEach { it.onNext(Unit) }
            }, onError = { it.printStackTrace() })

    private fun handleCypressOtaModeEntered() = completable {
        rootMessageChannel.disconnect()
        cypressOtaMessageChannel.connect(inputStream, outputStream)
        state.mode = CYPRESS_OTA
    }

    private fun handleStmOtaModeEntered() = completable {
        rootMessageChannel.disconnect()
        stmOtaMessageChannel.connect(inputStream, outputStream)
        state.mode = STM_OTA
    }

    fun getStmFirmwareVersion(): Single<StmFirmwareVersion> =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<GetStmFirmwareVersionResponse>(
                GetStmFirmwareVersionCommand()
            ))
            .map { it.stmFirmwareVersion }

    fun getUn20Status(): Single<Boolean> =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<GetUn20OnResponse>(
                GetUn20OnCommand()
            ))
            .map { it.value == DigitalValue.TRUE }
            .doOnSuccess { state.un20On = it }

    fun turnUn20OnAndAwaitStateChangeEvent(): Completable =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<SetUn20OnResponse>(
                SetUn20OnCommand(DigitalValue.TRUE)
            ).completeOnceReceived()
                .doSimultaneously(
                    mainMessageChannel.incoming.receiveResponse<Un20StateChangeEvent>(
                        withPredicate = { it.value == DigitalValue.TRUE }
                    )).handleErrorsWith(responseErrorHandler))
            .completeOnceReceived()
            .doOnComplete { state.un20On = true }

    fun turnUn20OffAndAwaitStateChangeEvent(): Completable =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<SetUn20OnResponse>(
                SetUn20OnCommand(DigitalValue.FALSE)
            ).completeOnceReceived()
                .doSimultaneously(mainMessageChannel.incoming.receiveResponse<Un20StateChangeEvent>(
                    withPredicate = { it.value == DigitalValue.FALSE }
                )).handleErrorsWith(responseErrorHandler))
            .completeOnceReceived()
            .doOnComplete { state.un20On = false }

    fun getTriggerButtonStatus(): Single<Boolean> =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<GetTriggerButtonActiveResponse>(
                GetTriggerButtonActiveCommand()
            ))
            .map { it.value == DigitalValue.TRUE }
            .doOnSuccess { state.triggerButtonActive = it }

    fun activateTriggerButton(): Completable =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<SetTriggerButtonActiveResponse>(
                SetTriggerButtonActiveCommand(DigitalValue.TRUE)
            ))
            .completeOnceReceived()
            .doOnComplete { state.triggerButtonActive = true }

    fun deactivateTriggerButton(): Completable =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<SetTriggerButtonActiveResponse>(
                SetTriggerButtonActiveCommand(DigitalValue.FALSE)
            ))
            .completeOnceReceived()
            .doOnComplete { state.triggerButtonActive = false }

    fun getSmileLedState(): Single<SmileLedState> =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<GetSmileLedStateResponse>(
                GetSmileLedStateCommand()
            ))
            .map { it.smileLedState }
            .doOnSuccess { state.smileLedState = it }

    fun getPowerLedState(): Single<LedState> =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<GetPowerLedStateResponse>(
                GetPowerLedStateCommand()
            ))
            .map { it.ledState }

    fun getBluetoothLedState(): Single<LedState> =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<GetBluetoothLedStateResponse>(
                GetBluetoothLedStateCommand()
            ))
            .map { it.ledState }

    fun setSmileLedState(smileLedState: SmileLedState): Completable =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<SetSmileLedStateResponse>(
                SetSmileLedStateCommand(smileLedState)
            ))
            .completeOnceReceived()
            .doOnComplete { state.smileLedState = smileLedState }

    fun getBatteryPercentCharge(): Single<Int> =
        assertConnected().andThen(assertMode(MAIN)).andThen(
            sendMainModeCommandAndReceiveResponse<GetBatteryPercentChargeResponse>(
                GetBatteryPercentChargeCommand()
            ))
            .map { it.batteryPercentCharge.percentCharge.unsignedToInt() }
            .doOnSuccess { state.batteryPercentCharge = it }

    fun getUn20AppVersion(): Single<Un20AppVersion> =
        assertConnected().andThen(assertMode(MAIN)).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetUn20AppVersionResponse>(
                GetUn20AppVersionCommand()
            ))
            .map { it.un20AppVersion }

    fun captureFingerprint(dpi: Dpi = DEFAULT_DPI): Single<CaptureFingerprintResult> =
        assertConnected().andThen(assertMode(MAIN)).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<CaptureFingerprintResponse>(
                CaptureFingerprintCommand(dpi)
            ))
            .map { it.captureFingerprintResult }

    fun getSupportedTemplateTypes(): Single<Set<TemplateType>> =
        assertConnected().andThen(assertMode(MAIN)).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetSupportedTemplateTypesResponse>(
                GetSupportedTemplateTypesCommand()
            ))
            .map { it.supportedTemplateTypes }

    fun acquireTemplate(templateType: TemplateType = DEFAULT_TEMPLATE_TYPE): Maybe<TemplateData> =
        assertConnected().andThen(assertMode(MAIN)).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetTemplateResponse>(
                GetTemplateCommand(templateType)
            ))
            .mapToMaybeEmptyIfNull { it.templateData }

    fun getSupportedImageFormats(): Single<Set<ImageFormat>> =
        assertConnected().andThen(assertMode(MAIN)).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetSupportedImageFormatsResponse>(
                GetSupportedImageFormatsCommand()
            ))
            .map { it.supportedImageFormats }

    fun acquireImage(imageFormatData: ImageFormatData = DEFAULT_IMAGE_FORMAT_DATA): Maybe<ImageData> =
        assertConnected().andThen(assertMode(MAIN)).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetImageResponse>(
                GetImageCommand(imageFormatData)
            ))
            .mapToMaybeEmptyIfNull { it.imageData }

    fun getImageQualityScore(): Maybe<Int> =
        assertConnected().andThen(assertMode(MAIN)).andThen(assertUn20On()).andThen(
            sendMainModeCommandAndReceiveResponse<GetImageQualityResponse>(
                GetImageQualityCommand()
            ))
            .mapToMaybeEmptyIfNull { it.imageQualityScore }

    /** @throws OtaFailedException If a domain error occurs at any step during the OTA process */
    fun startCypressOta(firmwareBinFile: ByteArray): Observable<Float> =
        assertConnected().andThen(assertMode(CYPRESS_OTA)).andThen(
            cypressOtaController.program(cypressOtaMessageChannel, responseErrorHandler, firmwareBinFile))

    /** @throws OtaFailedException If a domain error occurs at any step during the OTA process */
    fun startStmOta(firmwareBinFile: ByteArray): Observable<Float> =
        assertConnected().andThen(assertMode(STM_OTA)).andThen(
            stmOtaController.program(stmOtaMessageChannel, responseErrorHandler, firmwareBinFile))

    /** @throws OtaFailedException If a domain error occurs at any step during the OTA process */
    fun startUn20Ota(firmwareBinFile: ByteArray): Observable<Float> =
        assertConnected().andThen(assertMode(MAIN)).andThen(assertUn20On()).andThen(
            un20OtaController.program(mainMessageChannel, responseErrorHandler, firmwareBinFile))

    companion object {
        val DEFAULT_DPI = Dpi(500)
        val DEFAULT_TEMPLATE_TYPE = TemplateType.ISO_19794_2_2011
        val DEFAULT_IMAGE_FORMAT_DATA = ImageFormatData.RAW
    }
}

// This class uses entire vero API so import list would be extremely long without wildcards

package com.simprints.fingerprint.infra.scanner.v2.scanner

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.MainMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.RootMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode.CYPRESS_OTA
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode.MAIN
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode.ROOT
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode.STM_OTA
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.CaptureFingerprintCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageDistortionConfigurationMatrixCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageQualityCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageQualityPreviewCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetTemplateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetUnprocessedImageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.SetScanLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.TemplateData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageDistortionConfigurationMatrixResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageQualityPreviewResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageQualityResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetTemplateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.SetScanLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryCurrentCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryPercentChargeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryTemperatureCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryVoltageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetUn20OnCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.SetSmileLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.SmileLedState
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryCurrentResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryPercentChargeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryTemperatureResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryVoltageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterCypressOtaModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterStmOtaModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ScannerInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterCypressOtaModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterStmOtaModeResponse
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.IllegalUn20StateException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.IncorrectModeException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprint.infra.scanner.v2.scanner.ota.cypress.CypressOtaController
import com.simprints.fingerprint.infra.scanner.v2.scanner.ota.stm.StmOtaController
import com.simprints.fingerprint.infra.scanner.v2.scanner.ota.un20.Un20OtaController
import com.simprints.fingerprint.infra.scanner.v2.tools.asFlow
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.unsignedToInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.DigitalValue as Un20DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue as StmDigitalValue

/**
 * Methods in this class can throw various subclasses of these exceptions:
 * @throws IOException If disconnection or timeout occurs
 * @throws IllegalStateException On attempting to call certain methods whilst in an incorrect state
 * @throws IllegalArgumentException On receiving unexpected or invalid bytes from the scanner
 */
@Singleton
class Scanner @Inject constructor(
    private val mainMessageChannel: MainMessageChannel,
    private val rootMessageChannel: RootMessageChannel,
    private val scannerInfoReaderHelper: ScannerExtendedInfoReaderHelper,
    private val cypressOtaMessageChannel: CypressOtaMessageChannel,
    private val stmOtaMessageChannel: StmOtaMessageChannel,
    private val cypressOtaController: CypressOtaController,
    private val stmOtaController: StmOtaController,
    private val un20OtaController: Un20OtaController,
    private val scannerInfo: ScannerInfo,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) {
    private lateinit var outputStream: OutputStream
    private lateinit var inputStreamFlow: Flow<ByteArray>
    var state = disconnectedScannerState()

    lateinit var triggerButtonFlow: Flow<Unit>

    fun connect(
        inputStream: InputStream,
        outputStream: OutputStream,
    ) {
        inputStreamFlow = inputStream.asFlow(dispatcher = dispatcherIO)
        this.outputStream = outputStream
        state.connected = true
        state.mode = ROOT

        rootMessageChannel.connect(inputStreamFlow, outputStream)
    }

    fun disconnect() {
        // Disconnect scanner only when it is connected
        if (state.connected) {
            when (state.mode) {
                ROOT -> rootMessageChannel.disconnect()
                MAIN -> {
                    mainMessageChannel.disconnect()
                }

                CYPRESS_OTA -> cypressOtaMessageChannel.disconnect()
                STM_OTA -> stmOtaMessageChannel.disconnect()
                null -> { // Do nothing
                }
            }
            state = disconnectedScannerState()
            scannerInfo.clear()
        }
    }

    fun isConnected() = state.connected

    private fun assertConnected() {
        if (!state.connected) {
            throw NotConnectedException("Attempting to access functionality before calling Scanner::connect()")
        }
    }

    private fun assertMode(mode: Mode) {
        if (state.mode != mode) {
            throw IncorrectModeException("Attempting to access $mode functionality when current mode is ${state.mode}")
        }
    }

    private fun assertUn20On() {
        if (state.un20On != true) {
            throw IllegalUn20StateException("Attempting to access UN20 functionality when UN20 is off")
        }
    }

    suspend fun getVersionInformation(): ScannerInformation {
        assertConnected()
        assertMode(ROOT)
        return scannerInfoReaderHelper.readScannerInfo()
    }

    suspend fun setVersionInformation(versionInformation: ExtendedVersionInformation) {
        assertConnected()
        assertMode(ROOT)
        scannerInfoReaderHelper.setExtendedVersionInformation(versionInformation)
    }

    suspend fun getCypressFirmwareVersion(): CypressFirmwareVersion {
        assertConnected()
        assertMode(ROOT)
        return scannerInfoReaderHelper.getCypressVersion()
    }

    suspend fun getCypressExtendedFirmwareVersion(): CypressExtendedFirmwareVersion {
        assertConnected()
        assertMode(ROOT)
        return scannerInfoReaderHelper.getCypressExtendedVersion()
    }

    suspend fun enterMainMode() {
        assertConnected()
        assertMode(ROOT)
        rootMessageChannel.sendCommandAndReceiveResponse<EnterMainModeResponse>(
            EnterMainModeCommand(),
        )
        handleMainModeEntered()
    }

    suspend fun enterCypressOtaMode() {
        assertConnected()
        assertMode(ROOT)
        rootMessageChannel.sendCommandAndReceiveResponse<EnterCypressOtaModeResponse>(
            EnterCypressOtaModeCommand(),
        )
        handleCypressOtaModeEntered()
    }

    suspend fun enterStmOtaMode() {
        assertConnected()
        assertMode(ROOT)
        rootMessageChannel.sendCommandAndReceiveResponse<EnterStmOtaModeResponse>(
            EnterStmOtaModeCommand(),
        )
        handleStmOtaModeEntered()
    }

    private fun handleMainModeEntered() {
        rootMessageChannel.disconnect()
        mainMessageChannel.connect(inputStreamFlow, outputStream)
        state.triggerButtonActive = true
        state.mode = MAIN
        triggerButtonFlow = mainMessageChannel.incoming.veroEvents
            .filterIsInstance<TriggerButtonPressedEvent>()
            .map {
                // Trigger button pressed event received
            }
    }

    private fun handleCypressOtaModeEntered() {
        rootMessageChannel.disconnect()
        cypressOtaMessageChannel.connect(inputStreamFlow, outputStream)
        state.mode = CYPRESS_OTA
    }

    private fun handleStmOtaModeEntered() {
        rootMessageChannel.disconnect()
        stmOtaMessageChannel.connect(inputStreamFlow, outputStream)
        state.mode = STM_OTA
    }

    suspend fun getStmFirmwareVersion(): StmExtendedFirmwareVersion {
        assertConnected()
        assertMode(MAIN)
        return scannerInfoReaderHelper.getStmExtendedFirmwareVersion()
    }

    suspend fun getUn20Status(): Boolean {
        assertConnected()
        assertMode(MAIN)
        val un20Status = mainMessageChannel
            .sendCommandAndReceiveResponse<GetUn20OnResponse>(GetUn20OnCommand())
            .value == StmDigitalValue.TRUE
        state.un20On = un20Status
        return un20Status
    }

    suspend fun turnUn20On() {
        assertConnected()
        assertMode(MAIN)
        mainMessageChannel.sendCommandAndReceiveResponse<SetUn20OnResponse>(
            SetUn20OnCommand(StmDigitalValue.TRUE),
        )
        mainMessageChannel.receiveResponse<Un20StateChangeEvent>()
        state.un20On = true
    }

    suspend fun turnUn20Off() {
        assertConnected()
        assertMode(MAIN)
        mainMessageChannel.sendMainModeCommand(SetUn20OnCommand(StmDigitalValue.FALSE))
        mainMessageChannel.receiveResponse<Un20StateChangeEvent>()
        state.un20On = false
    }

    suspend fun setSmileLedState(smileLedState: SmileLedState) {
        if (smileLedState != state.smileLedState) {
            assertConnected()
            assertMode(MAIN)
            mainMessageChannel.sendMainModeCommand(SetSmileLedStateCommand(smileLedState))
            state.smileLedState = smileLedState
        }
    }

    suspend fun getBatteryPercentCharge(): Int {
        assertConnected()
        assertMode(MAIN)
        state.batteryPercentCharge =
            mainMessageChannel
                .sendCommandAndReceiveResponse<GetBatteryPercentChargeResponse>(
                    GetBatteryPercentChargeCommand(),
                ).batteryPercentCharge.percentCharge
                .unsignedToInt()
        return state.batteryPercentCharge!!
    }

    suspend fun getBatteryVoltageMilliVolts(): Int {
        assertConnected()
        assertMode(MAIN)
        state.batteryVoltageMilliVolts =
            mainMessageChannel
                .sendCommandAndReceiveResponse<GetBatteryVoltageResponse>(
                    GetBatteryVoltageCommand(),
                ).batteryVoltage.milliVolts
                .unsignedToInt()

        return state.batteryVoltageMilliVolts!!
    }

    suspend fun getBatteryCurrentMilliAmps(): Int {
        assertConnected()
        assertMode(MAIN)
        state.batteryCurrentMilliAmps =
            mainMessageChannel
                .sendCommandAndReceiveResponse<GetBatteryCurrentResponse>(
                    GetBatteryCurrentCommand(),
                ).batteryCurrent.milliAmps
                .toInt()
        return state.batteryCurrentMilliAmps!!
    }

    suspend fun getBatteryTemperatureDeciKelvin(): Int {
        assertConnected()
        assertMode(MAIN)
        state.batteryTemperatureDeciKelvin =
            mainMessageChannel
                .sendCommandAndReceiveResponse<GetBatteryTemperatureResponse>(
                    GetBatteryTemperatureCommand(),
                ).batteryTemperature.deciKelvin
                .unsignedToInt()
        return state.batteryTemperatureDeciKelvin!!
    }

    suspend fun getUn20AppVersion(): Un20ExtendedAppVersion {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return scannerInfoReaderHelper.getUn20ExtendedAppVersion()
    }

    suspend fun captureFingerprint(dpi: Dpi = DEFAULT_DPI): CaptureFingerprintResult {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return mainMessageChannel
            .sendCommandAndReceiveResponse<CaptureFingerprintResponse>(
                CaptureFingerprintCommand(dpi),
            ).captureFingerprintResult
    }

    /** Requires UN20 API 1.1 */
    suspend fun setScannerLedStateOn() {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        mainMessageChannel.sendCommandAndReceiveResponse<SetScanLedStateResponse>(
            SetScanLedStateCommand(Un20DigitalValue.TRUE),
        )
        state.scanLedState = true
    }

    /** Requires UN20 API 1.1 */
    suspend fun setScannerLedStateDefault() {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        mainMessageChannel.sendCommandAndReceiveResponse<SetScanLedStateResponse>(
            SetScanLedStateCommand(Un20DigitalValue.FALSE),
        )
        state.scanLedState = false
    }

    /** Requires UN20 API 1.1
     * No value emitted if an image could not be captured */
    suspend fun getImageQualityPreview(): Int? {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return mainMessageChannel
            .sendCommandAndReceiveResponse<GetImageQualityPreviewResponse>(
                GetImageQualityPreviewCommand(),
            ).imageQualityScore
    }

    /** No value emitted if an image has not been captured */
    suspend fun acquireTemplate(templateType: TemplateType = DEFAULT_TEMPLATE_TYPE): TemplateData? {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return mainMessageChannel
            .sendCommandAndReceiveResponse<GetTemplateResponse>(
                GetTemplateCommand(templateType),
            ).templateData
    }

    /** No value emitted if an image has not been captured */
    suspend fun acquireImage(imageFormatData: ImageFormatData = DEFAULT_IMAGE_FORMAT_DATA): ImageData? {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return mainMessageChannel
            .sendCommandAndReceiveResponse<GetImageResponse>(
                GetImageCommand(imageFormatData),
            ).imageData
    }

    suspend fun acquireUnprocessedImage(imageFormatData: ImageFormatData = DEFAULT_IMAGE_FORMAT_DATA): ImageData? {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return mainMessageChannel
            .sendCommandAndReceiveResponse<GetImageResponse>(
                GetUnprocessedImageCommand(imageFormatData),
            ).imageData
    }

    suspend fun acquireImageDistortionConfigurationMatrix(): ByteArray? {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return mainMessageChannel
            .sendCommandAndReceiveResponse<GetImageDistortionConfigurationMatrixResponse>(
                GetImageDistortionConfigurationMatrixCommand(),
            ).imageConfigurationMatrix
    }

    /** No value emitted if an image has not been captured */
    suspend fun getImageQualityScore(): Int? {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return mainMessageChannel
            .sendCommandAndReceiveResponse<GetImageQualityResponse>(
                GetImageQualityCommand(),
            ).imageQualityScore
    }

    /** @throws OtaFailedException If a domain error occurs at any step during the OTA process */
    suspend fun startCypressOta(firmwareBinFile: ByteArray): Flow<Float> {
        assertConnected()
        assertMode(CYPRESS_OTA)
        return cypressOtaController.program(
            cypressOtaMessageChannel,
            firmwareBinFile,
        )
    }

    /** @throws OtaFailedException If a domain error occurs at any step during the OTA process */
    suspend fun startStmOta(firmwareBinFile: ByteArray): Flow<Float> {
        assertConnected()
        assertMode(STM_OTA)
        return stmOtaController.program(
            stmOtaMessageChannel,
            firmwareBinFile,
        )
    }

    /** @throws OtaFailedException If a domain error occurs at any step during the OTA process */
    suspend fun startUn20Ota(firmwareBinFile: ByteArray): Flow<Float> {
        assertConnected()
        assertMode(MAIN)
        assertUn20On()
        return un20OtaController.program(
            mainMessageChannel,
            firmwareBinFile,
        )
    }

    companion object {
        val DEFAULT_DPI = Dpi(500)
        val DEFAULT_TEMPLATE_TYPE = TemplateType.ISO_19794_2_2011
        val DEFAULT_IMAGE_FORMAT_DATA = ImageFormatData.RAW
    }
}

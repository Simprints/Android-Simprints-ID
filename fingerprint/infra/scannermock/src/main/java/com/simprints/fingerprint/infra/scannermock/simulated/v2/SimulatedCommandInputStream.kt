package com.simprints.fingerprint.infra.scannermock.simulated.v2

import com.simprints.fingerprint.infra.scanner.v2.domain.Mode
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode.CYPRESS_OTA
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode.MAIN
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode.ROOT
import com.simprints.fingerprint.infra.scanner.v2.domain.Mode.STM_OTA
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.CaptureFingerprintCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageDistortionConfigurationMatrixCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageQualityCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageQualityPreviewCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetSupportedImageFormatsCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetSupportedTemplateTypesCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetTemplateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetUn20ExtendedAppVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetUnprocessedImageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.SetScanLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryCurrentCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryPercentChargeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryTemperatureCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryVoltageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBluetoothLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetPowerLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetSmileLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetStmExtendedFirmwareVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetTriggerButtonActiveCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetUn20OnCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.SetSmileLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.SetTriggerButtonActiveCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_BATTERY_CURRENT
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_BATTERY_PERCENT_CHARGE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_BATTERY_TEMPERATURE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_BATTERY_VOLTAGE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_BLUETOOTH_LED_STATE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_POWER_LED_STATE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_SMILE_LED_STATE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_STM_EXTENDED_FIRMWARE_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_TRIGGER_BUTTON_ACTIVE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.GET_UN20_ON
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.SET_SMILE_LED_STATE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.SET_TRIGGER_BUTTON_ACTIVE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.SET_UN20_ON
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.TRIGGER_BUTTON_PRESSED
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType.UN20_STATE_CHANGE
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.ENTER_CYPRESS_OTA_MODE
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.ENTER_MAIN_MODE
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.ENTER_STM_OTA_MODE
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_CYPRESS_EXTENDED_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_CYPRESS_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_EXTENDED_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_HARDWARE_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.SET_EXTENDED_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.SET_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterCypressOtaModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterStmOtaModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetCypressExtendedVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetCypressVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetExtendedVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetHardwareVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.SetExtendedVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.SetVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.PacketToMainMessageAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.toMainMessageStream
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.ByteArrayToPacketAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.PacketParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.ByteArrayAccumulator
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.accumulateAndTakeElements
import com.simprints.fingerprint.infra.scanner.v2.tools.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import java.io.PipedInputStream
import java.io.PipedOutputStream

class SimulatedCommandInputStream {
    private val rootOutputStream = PipedOutputStream()
    private val rootInputStream = PipedInputStream().also { it.connect(rootOutputStream) }

    private val mainOutputStream = PipedOutputStream()
    private val mainInputStream = PipedInputStream().also { it.connect(mainOutputStream) }
    private val dispatcher = Dispatchers.IO

    private val router =
        PacketRouter(
            listOf(Route.Remote.VeroServer, Route.Remote.Un20Server),
            { destination },
            ByteArrayToPacketAccumulator(PacketParser()),
            dispatcher,
        ).also { it.connect(mainInputStream.asFlow(dispatcher)) }

    val rootCommands: Flow<RootCommand> =
        rootInputStream
            .asFlow(dispatcher)
            .accumulateAndTakeElements(RootCommandAccumulator(RootCommandParser()))

    val veroCommands: Flow<VeroCommand> = router.incomingPacketRoutes
        .getValue(
            Route.Remote.VeroServer,
        ).toMainMessageStream(VeroCommandAccumulator(VeroCommandParser()))
    val un20Commands: Flow<Un20Command> = router.incomingPacketRoutes
        .getValue(
            Route.Remote.Un20Server,
        ).toMainMessageStream(Un20CommandAccumulator(Un20CommandParser()))

    fun disconnect() {
        router.disconnect()
    }

    fun updateWithNewBytes(
        bytes: ByteArray,
        mode: Mode,
    ) {
        when (mode) {
            ROOT -> rootOutputStream
            MAIN -> mainOutputStream
            CYPRESS_OTA -> throw UnsupportedOperationException("Simulated Scanner does not support Cypress OTA")
            STM_OTA -> throw UnsupportedOperationException("Simulated Scanner does not support STM OTA")
        }.apply {
            write(bytes)
            flush()
        }
    }

    class RootCommandAccumulator(
        rootCommandParser: RootCommandParser,
    ) : ByteArrayAccumulator<ByteArray, RootCommand>(
            fragmentAsByteArray = { it },
            canComputeElementLength = { bytes -> bytes.size >= RootMessageProtocol.HEADER_SIZE },
            computeElementLength = { bytes ->
                RootMessageProtocol.getTotalLengthFromHeader(bytes.sliceArray(RootMessageProtocol.HEADER_INDICES))
            },
            buildElement = { bytes -> rootCommandParser.parse(bytes) },
        )

    class VeroCommandAccumulator(
        veroCommandParser: VeroCommandParser,
    ) : PacketToMainMessageAccumulator<VeroCommand>(VeroMessageProtocol, veroCommandParser)

    class Un20CommandAccumulator(
        un20CommandParser: Un20CommandParser,
    ) : PacketToMainMessageAccumulator<Un20Command>(Un20MessageProtocol, un20CommandParser)

    class RootCommandParser : MessageParser<RootCommand> {
        override fun parse(messageBytes: ByteArray): RootCommand = RootMessageProtocol.getDataBytes(messageBytes).let { data ->
            when (RootMessageProtocol.getMessageType(messageBytes)) {
                ENTER_MAIN_MODE -> EnterMainModeCommand.fromBytes(data)
                ENTER_CYPRESS_OTA_MODE -> EnterCypressOtaModeCommand.fromBytes(data)
                ENTER_STM_OTA_MODE -> EnterStmOtaModeCommand.fromBytes(data)
                GET_CYPRESS_VERSION -> GetCypressVersionCommand.fromBytes(data)
                GET_VERSION -> GetVersionCommand.fromBytes(data)
                SET_VERSION -> SetVersionCommand.fromBytes(data)

                GET_EXTENDED_VERSION -> GetExtendedVersionCommand.fromBytes(data)
                GET_HARDWARE_VERSION -> GetHardwareVersionCommand.fromBytes(data)
                GET_CYPRESS_EXTENDED_VERSION -> GetCypressExtendedVersionCommand.fromBytes(data)
                SET_EXTENDED_VERSION -> SetExtendedVersionCommand.fromBytes(data)
            }
        }
    }

    class VeroCommandParser : MessageParser<VeroCommand> {
        override fun parse(messageBytes: ByteArray): VeroCommand = VeroMessageProtocol.getDataBytes(messageBytes).let { data ->
            when (VeroMessageProtocol.getMessageType(messageBytes)) {
                GET_STM_EXTENDED_FIRMWARE_VERSION -> GetStmExtendedFirmwareVersionCommand.fromBytes(data)
                GET_UN20_ON -> GetUn20OnCommand.fromBytes(data)
                SET_UN20_ON -> SetUn20OnCommand.fromBytes(data)
                GET_TRIGGER_BUTTON_ACTIVE -> GetTriggerButtonActiveCommand.fromBytes(data)
                SET_TRIGGER_BUTTON_ACTIVE -> SetTriggerButtonActiveCommand.fromBytes(data)
                GET_SMILE_LED_STATE -> GetSmileLedStateCommand.fromBytes(data)
                GET_BLUETOOTH_LED_STATE -> GetBluetoothLedStateCommand.fromBytes(data)
                GET_POWER_LED_STATE -> GetPowerLedStateCommand.fromBytes(data)
                SET_SMILE_LED_STATE -> SetSmileLedStateCommand.fromBytes(data)
                GET_BATTERY_PERCENT_CHARGE -> GetBatteryPercentChargeCommand.fromBytes(data)
                GET_BATTERY_VOLTAGE -> GetBatteryVoltageCommand.fromBytes(data)
                GET_BATTERY_CURRENT -> GetBatteryCurrentCommand.fromBytes(data)
                GET_BATTERY_TEMPERATURE -> GetBatteryTemperatureCommand.fromBytes(data)
                UN20_STATE_CHANGE, TRIGGER_BUTTON_PRESSED -> throw IllegalArgumentException("Should not send events")
            }
        }
    }

    class Un20CommandParser : MessageParser<Un20Command> {
        override fun parse(messageBytes: ByteArray): Un20Command = Pair(
            Un20MessageProtocol.getMinorTypeByte(messageBytes),
            Un20MessageProtocol.getDataBytes(messageBytes),
        ).let { (minorTypeByte, data) ->
            when (Un20MessageProtocol.getMessageType(messageBytes)) {
                Un20MessageType.GetUn20ExtendedAppVersion -> GetUn20ExtendedAppVersionCommand.fromBytes(data)
                Un20MessageType.CaptureFingerprint -> CaptureFingerprintCommand.fromBytes(data)
                Un20MessageType.GetImageQualityPreview -> GetImageQualityPreviewCommand.fromBytes(data)
                Un20MessageType.SetScanLedState -> SetScanLedStateCommand.fromBytes(data)
                Un20MessageType.GetSupportedTemplateTypes -> GetSupportedTemplateTypesCommand.fromBytes(data)
                is Un20MessageType.GetTemplate -> GetTemplateCommand.fromBytes(minorTypeByte, data)
                Un20MessageType.GetSupportedImageFormats -> GetSupportedImageFormatsCommand.fromBytes(data)
                is Un20MessageType.GetImage -> GetImageCommand.fromBytes(minorTypeByte, data)
                Un20MessageType.GetImageQuality -> GetImageQualityCommand.fromBytes(data)
                is Un20MessageType.GetUnprocessedImage -> GetUnprocessedImageCommand.fromBytes(minorTypeByte, data)
                Un20MessageType.GetImageDistortionConfigurationMatrix -> GetImageDistortionConfigurationMatrixCommand.fromBytes(data)
                Un20MessageType.StartOta,
                Un20MessageType.WriteOtaChunk,
                Un20MessageType.VerifyOta,
                -> throw UnsupportedOperationException("Simulated Scanner does not support UN20 OTA")
            }
        }
    }
}

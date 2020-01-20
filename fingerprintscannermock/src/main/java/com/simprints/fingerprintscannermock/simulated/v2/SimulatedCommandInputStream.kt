package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.*
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.*
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType.*
import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.PacketToMessageAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.MessageParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.toMessageStream
import com.simprints.fingerprintscanner.v2.incoming.main.packet.ByteArrayToPacketAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketParser
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketRouter
import io.reactivex.Flowable
import java.io.PipedInputStream
import java.io.PipedOutputStream

class SimulatedCommandInputStream {

    private val outputStream = PipedOutputStream()
    private val inputStream = PipedInputStream().also { it.connect(outputStream) }

    private val router =
        PacketRouter(
            listOf(Channel.Remote.VeroServer, Channel.Remote.Un20Server),
            { destination },
            ByteArrayToPacketAccumulator(PacketParser())
        ).also { it.connect(inputStream) }

    val veroCommands: Flowable<VeroCommand> = router.incomingPacketChannels[Channel.Remote.VeroServer]?.toMessageStream(VeroCommandAccumulator(VeroCommandParser()))
        ?: throw IllegalStateException()
    val un20Commands: Flowable<Un20Command> = router.incomingPacketChannels[Channel.Remote.Un20Server]?.toMessageStream(Un20CommandAccumulator(Un20CommandParser()))
        ?: throw IllegalStateException()

    fun updateWithNewBytes(bytes: ByteArray) {
        outputStream.write(bytes)
        outputStream.flush()
    }

    class VeroCommandAccumulator(veroCommandParser: VeroCommandParser) : PacketToMessageAccumulator<VeroCommand>(VeroMessageProtocol, veroCommandParser)

    class Un20CommandAccumulator(un20CommandParser: Un20CommandParser) : PacketToMessageAccumulator<Un20Command>(Un20MessageProtocol, un20CommandParser)

    class VeroCommandParser : MessageParser<VeroCommand> {

        override fun parse(messageBytes: ByteArray): VeroCommand =
            VeroMessageProtocol.getDataBytes(messageBytes).let { data ->
                when (VeroMessageProtocol.getMessageType(messageBytes)) {
                    GET_STM_FIRMWARE_VERSION -> GetStmFirmwareVersionCommand.fromBytes(data)
                    GET_UN20_ON -> GetUn20OnCommand.fromBytes(data)
                    SET_UN20_ON -> SetUn20OnCommand.fromBytes(data)
                    GET_TRIGGER_BUTTON_ACTIVE -> GetTriggerButtonActiveCommand.fromBytes(data)
                    SET_TRIGGER_BUTTON_ACTIVE -> SetTriggerButtonActiveCommand.fromBytes(data)
                    GET_SMILE_LED_STATE -> GetSmileLedStateCommand.fromBytes(data)
                    GET_BLUETOOTH_LED_STATE -> GetBluetoothLedStateCommand.fromBytes(data)
                    GET_POWER_LED_STATE -> GetPowerLedStateCommand.fromBytes(data)
                    SET_SMILE_LED_STATE -> SetSmileLedStateCommand.fromBytes(data)
                    GET_BATTERY_PERCENT_CHARGE -> GetBatteryPercentChargeCommand.fromBytes(data)
                    UN20_STATE_CHANGE, TRIGGER_BUTTON_PRESSED -> throw IllegalArgumentException("Should not send events")
                }
            }
    }

    class Un20CommandParser : MessageParser<Un20Command> {

        override fun parse(messageBytes: ByteArray): Un20Command =
            Pair(
                Un20MessageProtocol.getMinorTypeByte(messageBytes),
                Un20MessageProtocol.getDataBytes(messageBytes)
            ).let { (minorTypeByte, data) ->
                when (Un20MessageProtocol.getMessageType(messageBytes)) {
                    Un20MessageType.GetUn20AppVersion -> GetUn20AppVersionCommand.fromBytes(data)
                    Un20MessageType.CaptureFingerprint -> CaptureFingerprintCommand.fromBytes(data)
                    Un20MessageType.GetSupportedTemplateTypes -> GetSupportedTemplateTypesCommand.fromBytes(data)
                    is Un20MessageType.GetTemplate -> GetTemplateCommand.fromBytes(minorTypeByte, data)
                    Un20MessageType.GetSupportedImageFormats -> GetSupportedImageFormatsCommand.fromBytes(data)
                    is Un20MessageType.GetImage -> GetImageCommand.fromBytes(minorTypeByte, data)
                    Un20MessageType.GetImageQuality -> GetImageQualityCommand.fromBytes(data)
                }
            }
    }
}

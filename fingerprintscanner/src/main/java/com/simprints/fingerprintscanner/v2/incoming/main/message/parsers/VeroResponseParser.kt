package com.simprints.fingerprintscanner.v2.incoming.main.message.parsers

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType.*
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses.*
import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprintscanner.v2.incoming.MessageParser

class VeroResponseParser : MessageParser<VeroResponse> {

    override fun parse(messageBytes: ByteArray): VeroResponse =
        try {
            VeroMessageProtocol.getDataBytes(messageBytes).let { data ->
                when (val type = VeroMessageProtocol.getMessageType(messageBytes)) {
                    GET_STM_FIRMWARE_VERSION -> GetStmFirmwareVersionResponse.fromBytes(data)
                    GET_UN20_ON -> GetUn20OnResponse.fromBytes(data)
                    SET_UN20_ON -> SetUn20OnResponse.fromBytes(data)
                    GET_TRIGGER_BUTTON_ACTIVE -> GetTriggerButtonActiveResponse.fromBytes(data)
                    SET_TRIGGER_BUTTON_ACTIVE -> SetTriggerButtonActiveResponse.fromBytes(data)
                    GET_SMILE_LED_STATE -> GetSmileLedStateResponse.fromBytes(data)
                    GET_BLUETOOTH_LED_STATE -> GetBluetoothLedStateResponse.fromBytes(data)
                    GET_POWER_LED_STATE -> GetPowerLedStateResponse.fromBytes(data)
                    SET_SMILE_LED_STATE -> SetSmileLedStateResponse.fromBytes(data)
                    GET_BATTERY_PERCENT_CHARGE -> GetBatteryPercentChargeResponse.fromBytes(data)
                    UN20_STATE_CHANGE, TRIGGER_BUTTON_PRESSED ->
                        throw InvalidMessageException("Illegal message $type received in Vero events channel")
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            throw InvalidMessageException("Incorrect number of bytes received parsing Vero response", e)
        }
}

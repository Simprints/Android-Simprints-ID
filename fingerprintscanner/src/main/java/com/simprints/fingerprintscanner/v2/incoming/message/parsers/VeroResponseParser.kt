package com.simprints.fingerprintscanner.v2.incoming.message.parsers

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.*
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType.*

class VeroResponseParser : MessageParser<VeroResponse> {

    override fun parse(messageBytes: ByteArray): VeroResponse =
        VeroMessageProtocol.getDataBytes(messageBytes).let { data ->
            when (VeroMessageProtocol.getMessageType(messageBytes)) {
                GET_FIRMWARE_VERSION -> GetFirmwareVersionResponse.fromBytes(data)
                GET_UN20_ON -> GetUn20OnResponse.fromBytes(data)
                SET_UN20_ON -> SetUn20OnResponse.fromBytes(data)
                GET_TRIGGER_BUTTON_ACTIVE -> GetTriggerButtonActiveResponse.fromBytes(data)
                SET_TRIGGER_BUTTON_ACTIVE -> SetTriggerButtonActiveResponse.fromBytes(data)
                GET_SMILE_LED_STATE -> GetSmileLedStateResponse.fromBytes(data)
                GET_BLUETOOTH_LED_STATE -> GetBluetoothLedStateResponse.fromBytes(data)
                GET_POWER_LED_STATE -> GetPowerLedStateResponse.fromBytes(data)
                SET_SMILE_LED_STATE -> GetSmileLedStateResponse.fromBytes(data)
                SET_BLUETOOTH_LED_STATE -> SetBluetoothLedStateResponse.fromBytes(data)
                SET_POWER_LED_STATE -> SetPowerLedStateResponse.fromBytes(data)
                UN20_STATE_CHANGE, TRIGGER_BUTTON_PRESSED -> TODO("exception handling")
            }
        }
}

package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
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
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryCurrentResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryPercentChargeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryTemperatureResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryVoltageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBluetoothLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetPowerLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetSmileLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetStmExtendedFirmwareVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetTriggerButtonActiveResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetSmileLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetTriggerButtonActiveResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import javax.inject.Inject

class VeroResponseParser @Inject constructor() : MessageParser<VeroResponse> {
    override fun parse(messageBytes: ByteArray): VeroResponse = try {
        VeroMessageProtocol.getDataBytes(messageBytes).let { data ->
            when (val type = VeroMessageProtocol.getMessageType(messageBytes)) {
                GET_UN20_ON -> GetUn20OnResponse.fromBytes(data)
                SET_UN20_ON -> SetUn20OnResponse.fromBytes(data)
                GET_TRIGGER_BUTTON_ACTIVE -> GetTriggerButtonActiveResponse.fromBytes(data)
                SET_TRIGGER_BUTTON_ACTIVE -> SetTriggerButtonActiveResponse.fromBytes(data)
                GET_SMILE_LED_STATE -> GetSmileLedStateResponse.fromBytes(data)
                GET_BLUETOOTH_LED_STATE -> GetBluetoothLedStateResponse.fromBytes(data)
                GET_POWER_LED_STATE -> GetPowerLedStateResponse.fromBytes(data)
                SET_SMILE_LED_STATE -> SetSmileLedStateResponse.fromBytes(data)
                GET_BATTERY_PERCENT_CHARGE -> GetBatteryPercentChargeResponse.fromBytes(data)
                GET_BATTERY_VOLTAGE -> GetBatteryVoltageResponse.fromBytes(data)
                GET_BATTERY_CURRENT -> GetBatteryCurrentResponse.fromBytes(data)
                GET_BATTERY_TEMPERATURE -> GetBatteryTemperatureResponse.fromBytes(data)

                // extension api
                GET_STM_EXTENDED_FIRMWARE_VERSION -> GetStmExtendedFirmwareVersionResponse.fromBytes(data)

                UN20_STATE_CHANGE, TRIGGER_BUTTON_PRESSED ->
                    throw InvalidMessageException("Illegal message $type received in Vero events route")
            }
        }
    } catch (e: Exception) {
        handleExceptionDuringParsing(e)
    }
}

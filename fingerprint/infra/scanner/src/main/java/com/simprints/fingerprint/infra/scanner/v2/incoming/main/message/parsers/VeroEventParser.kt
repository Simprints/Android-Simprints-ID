package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType
import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import javax.inject.Inject

class VeroEventParser @Inject constructor() : MessageParser<VeroEvent> {
    override fun parse(messageBytes: ByteArray): VeroEvent = try {
        VeroMessageProtocol.getDataBytes(messageBytes).let { data ->
            when (val type = VeroMessageProtocol.getMessageType(messageBytes)) {
                VeroMessageType.UN20_STATE_CHANGE -> Un20StateChangeEvent.fromBytes(data)
                VeroMessageType.TRIGGER_BUTTON_PRESSED -> TriggerButtonPressedEvent.fromBytes(data)
                VeroMessageType.GET_STM_EXTENDED_FIRMWARE_VERSION,
                VeroMessageType.GET_UN20_ON,
                VeroMessageType.SET_UN20_ON,
                VeroMessageType.GET_TRIGGER_BUTTON_ACTIVE,
                VeroMessageType.SET_TRIGGER_BUTTON_ACTIVE,
                VeroMessageType.GET_SMILE_LED_STATE,
                VeroMessageType.GET_BLUETOOTH_LED_STATE,
                VeroMessageType.GET_POWER_LED_STATE,
                VeroMessageType.SET_SMILE_LED_STATE,
                VeroMessageType.GET_BATTERY_PERCENT_CHARGE,
                VeroMessageType.GET_BATTERY_VOLTAGE,
                VeroMessageType.GET_BATTERY_CURRENT,
                VeroMessageType.GET_BATTERY_TEMPERATURE,
                ->
                    throw InvalidMessageException("Illegal message $type received in Vero events route")
            }
        }
    } catch (e: Exception) {
        handleExceptionDuringParsing(e)
    }
}

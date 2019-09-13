package com.simprints.fingerprintscanner.v2.incoming.message.parsers

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.message.vero.messages.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.messages.events.Un20StateChangeEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class VeroEventParser : MessageParser<VeroEvent> {

    override fun parse(bytes: ByteArray): VeroEvent =
        VeroMessageProtocol.getDataBytes(bytes).let { data ->
            when (VeroMessageProtocol.getMessageType(bytes)) {
                VeroMessageType.UN20_STATE_CHANGE -> Un20StateChangeEvent.fromBytes(data)
                VeroMessageType.TRIGGER_BUTTON_PRESSED -> TriggerButtonPressedEvent.fromBytes(data)
                VeroMessageType.GET_FIRMWARE_VERSION,
                VeroMessageType.GET_UN20_ON,
                VeroMessageType.SET_UN20_ON,
                VeroMessageType.GET_TRIGGER_BUTTON_ACTIVE,
                VeroMessageType.SET_TRIGGER_BUTTON_ACTIVE,
                VeroMessageType.GET_SMILE_LED_STATE,
                VeroMessageType.GET_BLUETOOTH_LED_STATE,
                VeroMessageType.GET_POWER_LED_STATE,
                VeroMessageType.SET_SMILE_LED_STATE,
                VeroMessageType.SET_BLUETOOTH_LED_STATE,
                VeroMessageType.SET_POWER_LED_STATE -> TODO()
            }
        }
}

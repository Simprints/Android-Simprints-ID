package com.simprints.fingerprintscanner.v2.domain.message.vero.events

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class TriggerButtonPressedEvent: VeroEvent(VeroMessageType.TRIGGER_BUTTON_PRESSED) {

    companion object {
        fun fromBytes(@Suppress("UNUSED_PARAMETER") data: ByteArray) =
            TriggerButtonPressedEvent()
    }
}

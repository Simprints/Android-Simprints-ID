package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class SetTriggerButtonActiveCommand(val value: DigitalValue) : VeroCommand(VeroMessageType.SET_TRIGGER_BUTTON_ACTIVE) {

    override fun getDataBytes(): ByteArray = byteArrayOf(value.byte)

    companion object {
        fun fromBytes(data: ByteArray) = SetTriggerButtonActiveCommand(DigitalValue.fromBytes(data))
    }
}

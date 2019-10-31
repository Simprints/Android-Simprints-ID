package com.simprints.fingerprintscanner.v2.domain.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetTriggerButtonActiveCommand(val value: DigitalValue) : VeroCommand(VeroMessageType.SET_TRIGGER_BUTTON_ACTIVE) {

    override fun getDataBytes(): ByteArray = byteArrayOf(value.byte)

    companion object {
        fun fromBytes(data: ByteArray) = SetTriggerButtonActiveCommand(DigitalValue.fromBytes(data))
    }
}

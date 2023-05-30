package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class SetUn20OnCommand(val value: DigitalValue) : VeroCommand(VeroMessageType.SET_UN20_ON) {

    override fun getDataBytes(): ByteArray = byteArrayOf(value.byte)

    companion object {
        fun fromBytes(data: ByteArray) = SetUn20OnCommand(DigitalValue.fromBytes(data))
    }
}

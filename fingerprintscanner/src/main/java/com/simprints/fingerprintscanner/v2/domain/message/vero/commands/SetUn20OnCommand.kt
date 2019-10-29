package com.simprints.fingerprintscanner.v2.domain.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetUn20OnCommand(val value: DigitalValue) : VeroCommand(VeroMessageType.SET_UN20_ON) {

    override fun getDataBytes(): ByteArray = byteArrayOf(value.byte)
}

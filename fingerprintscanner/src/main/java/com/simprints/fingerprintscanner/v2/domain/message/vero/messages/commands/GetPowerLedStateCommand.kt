package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetPowerLedStateCommand : VeroCommand(VeroMessageType.GET_POWER_LED_STATE)

package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetFirmwareVersionCommand : VeroCommand(VeroMessageType.GET_FIRMWARE_VERSION)

package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.FirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetFirmwareVersionResponse(val firmwareVersion: FirmwareVersion) : VeroResponse(VeroMessageType.GET_FIRMWARE_VERSION)

package com.simprints.fingerprintscanner.v2.domain.message.un20.messages.commands

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.ImageFormat
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class GetImageCommand(val imageFormat: ImageFormat) : Un20Command(Un20MessageType.GetImage(imageFormat.byte))

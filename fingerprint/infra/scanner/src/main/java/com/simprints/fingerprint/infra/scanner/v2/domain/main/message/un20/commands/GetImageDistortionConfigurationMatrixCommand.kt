package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

/**
 * Get raw image distortion matrix configuration
 * Returns the UN20 calibration file (sgdevun20a.cfg) which can be used by Secugen Android library
 */
class GetImageDistortionConfigurationMatrixCommand : Un20Command(Un20MessageType.GetImageDistortionConfigurationMatrix) {
    companion object {
        fun fromBytes(
            @Suppress("unused_parameter") data: ByteArray,
        ) = GetImageDistortionConfigurationMatrixCommand()
    }
}

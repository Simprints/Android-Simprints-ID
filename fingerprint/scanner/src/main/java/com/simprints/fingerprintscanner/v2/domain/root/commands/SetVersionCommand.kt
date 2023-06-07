package com.simprints.fingerprintscanner.v2.domain.root.commands

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootMessageType
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation

class SetVersionCommand(val version: UnifiedVersionInformation) : RootCommand(RootMessageType.SET_VERSION) {

    override fun getDataBytes(): ByteArray = version.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = SetVersionCommand(
            UnifiedVersionInformation.fromBytes(data)
        )
    }
}

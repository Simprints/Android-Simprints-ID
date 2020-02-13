package com.simprints.fingerprintscanner.v2.outgoing.root

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.outgoing.common.MessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.common.OutputStreamDispatcher

class RootMessageOutputStream(
    rootMessageSerializer: RootMessageSerializer,
    outputStreamDispatcher: OutputStreamDispatcher
) : MessageOutputStream<RootCommand>(rootMessageSerializer, outputStreamDispatcher)

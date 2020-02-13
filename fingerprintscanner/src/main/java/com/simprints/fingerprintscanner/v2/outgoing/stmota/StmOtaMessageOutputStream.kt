package com.simprints.fingerprintscanner.v2.outgoing.stmota

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.outgoing.common.MessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.common.OutputStreamDispatcher

class StmOtaMessageOutputStream(
    stmOtaMessageSerializer: StmOtaMessageSerializer,
    outputStreamDispatcher: OutputStreamDispatcher
) : MessageOutputStream<StmOtaCommand>(stmOtaMessageSerializer, outputStreamDispatcher)

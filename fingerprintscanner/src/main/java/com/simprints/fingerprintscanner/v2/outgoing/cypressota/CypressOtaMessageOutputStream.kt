package com.simprints.fingerprintscanner.v2.outgoing.cypressota

import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprintscanner.v2.outgoing.common.MessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.common.OutputStreamDispatcher

class CypressOtaMessageOutputStream(
    cypressOtaMessageSerializer: CypressOtaMessageSerializer,
    outputStreamDispatcher: OutputStreamDispatcher
) : MessageOutputStream<CypressOtaCommand>(cypressOtaMessageSerializer, outputStreamDispatcher)

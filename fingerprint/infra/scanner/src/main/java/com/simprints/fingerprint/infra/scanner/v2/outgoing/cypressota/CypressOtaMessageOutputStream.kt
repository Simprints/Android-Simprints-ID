package com.simprints.fingerprint.infra.scanner.v2.outgoing.cypressota

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.MessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import javax.inject.Inject

class CypressOtaMessageOutputStream @Inject constructor(
    cypressOtaMessageSerializer: CypressOtaMessageSerializer,
    outputStreamDispatcher: OutputStreamDispatcher,
) : MessageOutputStream<CypressOtaCommand>(cypressOtaMessageSerializer, outputStreamDispatcher)

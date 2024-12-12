package com.simprints.fingerprint.infra.scanner.v2.outgoing.main

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.MessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import javax.inject.Inject

class MainMessageOutputStream @Inject constructor(
    mainMessageSerializer: MainMessageSerializer,
    outputStreamDispatcher: OutputStreamDispatcher,
) : MessageOutputStream<OutgoingMainMessage>(mainMessageSerializer, outputStreamDispatcher)

package com.simprints.fingerprint.infra.scanner.v2.domain.main.message

import com.simprints.fingerprint.infra.scanner.v2.domain.IncomingMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.Message
import com.simprints.fingerprint.infra.scanner.v2.domain.OutgoingMessage

interface MainMessage : Message

interface OutgoingMainMessage :
    MainMessage,
    OutgoingMessage

interface IncomingMainMessage :
    MainMessage,
    IncomingMessage

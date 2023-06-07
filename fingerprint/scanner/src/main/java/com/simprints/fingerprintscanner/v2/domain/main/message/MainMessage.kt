package com.simprints.fingerprintscanner.v2.domain.main.message

import com.simprints.fingerprintscanner.v2.domain.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.Message
import com.simprints.fingerprintscanner.v2.domain.OutgoingMessage

interface MainMessage : Message

interface OutgoingMainMessage: MainMessage, OutgoingMessage

interface IncomingMainMessage: MainMessage, IncomingMessage

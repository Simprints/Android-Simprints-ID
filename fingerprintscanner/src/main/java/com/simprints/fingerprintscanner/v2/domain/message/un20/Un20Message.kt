package com.simprints.fingerprintscanner.v2.domain.message.un20

import com.simprints.fingerprintscanner.v2.domain.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.message.Message
import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage

sealed class Un20Message : Message
abstract class Un20Command : Un20Message(), OutgoingMessage
abstract class Un20Response : Un20Message(), IncomingMessage

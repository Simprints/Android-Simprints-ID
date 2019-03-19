package com.simprints.id.exceptions.safe.sync

import com.simprints.id.exceptions.safe.SafeException


class InterruptedSyncException(message: String = "InterruptedSyncException")
    : SafeException(message)

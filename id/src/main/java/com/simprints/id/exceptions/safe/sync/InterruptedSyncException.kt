package com.simprints.id.exceptions.safe.sync

import com.simprints.id.exceptions.safe.SimprintsException


class InterruptedSyncException(message: String = "InterruptedSyncException")
    : SimprintsException(message)

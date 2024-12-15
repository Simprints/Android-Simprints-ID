package com.simprints.infra.eventsync.exceptions

internal class MalformedSyncOperationException(
    message: String = "People sync operation is malformed as input for worker",
) : IllegalStateException(message)

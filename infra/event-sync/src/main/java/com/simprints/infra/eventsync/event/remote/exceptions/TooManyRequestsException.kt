package com.simprints.infra.eventsync.event.remote.exceptions

internal class TooManyRequestsException(
    message: String = "TooManyRequest",
) : RuntimeException(message)

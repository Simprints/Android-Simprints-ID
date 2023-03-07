package com.simprints.infra.eventsync.event.remote.exceptions

class TooManyRequestsException(message: String = "TooManyRequest") : RuntimeException(message)

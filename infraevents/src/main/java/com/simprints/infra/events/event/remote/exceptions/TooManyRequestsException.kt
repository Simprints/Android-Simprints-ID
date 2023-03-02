package com.simprints.infra.events.remote.exceptions

class TooManyRequestsException(message: String = "TooManyRequest") : RuntimeException(message)

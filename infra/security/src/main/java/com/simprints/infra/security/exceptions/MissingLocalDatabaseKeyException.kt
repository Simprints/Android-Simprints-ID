package com.simprints.infra.security.exceptions

internal class MissingLocalDatabaseKeyException(
    message: String = "MissingLocalDatabaseKeyException",
) : RuntimeException(message)

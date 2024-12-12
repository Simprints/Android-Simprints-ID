package com.simprints.infra.security.exceptions

internal class MissingLocalDatabaseKeyHashException(
    message: String = "MissingLocalDatabaseKeyHashException",
) : RuntimeException(message)

package com.simprints.infra.security.exceptions

internal class MatchingLocalDatabaseKeyHashesException(
    message: String = "Saved key hash is the same as the one calculated with the current key",
) : RuntimeException(message)

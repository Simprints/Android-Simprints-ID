package com.simprints.infra.security.exceptions

internal class MismatchingLocalDatabaseKeyHashesException(
    message: String = "Saved key hash is different from the one calculated with the current key",
) : RuntimeException(message)

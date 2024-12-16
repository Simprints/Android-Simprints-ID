package com.simprints.infra.eventsync.exceptions

internal class MissingArgumentForDownSyncScopeException(
    message: String = "MissingArgumentForDownSyncScopeException",
) : IllegalStateException(message)

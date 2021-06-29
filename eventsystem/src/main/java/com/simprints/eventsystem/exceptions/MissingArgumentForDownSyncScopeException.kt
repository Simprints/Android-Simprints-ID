package com.simprints.eventsystem.exceptions

import com.simprints.core.exceptions.UnexpectedException


class MissingArgumentForDownSyncScopeException(message: String = "MissingArgumentForDownSyncScopeException")
    : UnexpectedException(message)

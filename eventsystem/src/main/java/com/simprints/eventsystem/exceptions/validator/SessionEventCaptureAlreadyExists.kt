package com.simprints.eventsystem.exceptions.validator

import com.simprints.eventsystem.exceptions.SessionDataSourceException

class SessionEventCaptureAlreadyExists(message: String): SessionDataSourceException(message)

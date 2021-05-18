package com.simprints.eventsystem.exceptions.validator

import com.simprints.id.exceptions.safe.session.SessionDataSourceException

class SessionEventCaptureAlreadyExists(message: String): SessionDataSourceException(message)

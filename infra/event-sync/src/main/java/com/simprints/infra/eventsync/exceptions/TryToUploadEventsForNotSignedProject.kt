package com.simprints.infra.eventsync.exceptions

import com.simprints.core.exceptions.SafeException

internal class TryToUploadEventsForNotSignedProject(
    message: String = "TryToUploadEventsForNotSignedProject"
) : SafeException(message)

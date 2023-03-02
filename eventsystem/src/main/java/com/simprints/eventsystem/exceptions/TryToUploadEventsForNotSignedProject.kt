package com.simprints.eventsystem.exceptions

import com.simprints.core.exceptions.SafeException

internal class TryToUploadEventsForNotSignedProject(
    message: String = "TryToUploadEventsForNotSignedProject"
) : SafeException(message)

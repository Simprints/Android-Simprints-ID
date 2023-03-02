package com.simprints.infra.events.exceptions

import com.simprints.core.exceptions.SafeException

internal class TryToUploadEventsForNotSignedProject(
    message: String = "TryToUploadEventsForNotSignedProject"
) : SafeException(message)

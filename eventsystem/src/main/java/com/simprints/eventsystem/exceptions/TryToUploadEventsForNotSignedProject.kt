package com.simprints.eventsystem.exceptions

import com.simprints.core.exceptions.SafeException

class TryToUploadEventsForNotSignedProject(message: String = "TryToUploadEventsForNotSignedProject") :
    SafeException(message)

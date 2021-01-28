package com.simprints.id.exceptions.safe.sync

import com.simprints.id.exceptions.safe.SafeException

class TryToUploadEventsForNotSignedProject(message: String = "TryToUploadEventsForNotSignedProject") :
    SafeException(message)

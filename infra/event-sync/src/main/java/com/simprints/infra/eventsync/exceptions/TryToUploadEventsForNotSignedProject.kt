package com.simprints.infra.eventsync.exceptions

internal class TryToUploadEventsForNotSignedProject(
    message: String = "TryToUploadEventsForNotSignedProject",
) : Exception(message)

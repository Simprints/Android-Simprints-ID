package com.simprints.id.exceptions.safe

import com.simprints.core.exceptions.SafeException


class SimprintsInternalServerException(message: String = "SimprintsInternalServerException")
    : SafeException(message)

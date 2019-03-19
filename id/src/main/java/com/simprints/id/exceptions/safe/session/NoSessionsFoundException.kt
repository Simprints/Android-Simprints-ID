package com.simprints.id.exceptions.safe.session

import com.simprints.id.exceptions.safe.SafeException

class NoSessionsFoundException(message: String = "NoSessionsFoundException") : SafeException(message)

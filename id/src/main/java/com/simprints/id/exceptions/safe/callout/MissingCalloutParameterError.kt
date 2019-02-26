package com.simprints.id.exceptions.safe.callout

import com.simprints.id.exceptions.safe.SafeException

class MissingCalloutParameterError(message: String = "MissingCalloutParameterError")
    : SafeException(message)

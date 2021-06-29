package com.simprints.id.exceptions.safe

import com.simprints.core.exceptions.SafeException

class CredentialMissingException(message: String = "CredentialMissingException") :
    SafeException(message)

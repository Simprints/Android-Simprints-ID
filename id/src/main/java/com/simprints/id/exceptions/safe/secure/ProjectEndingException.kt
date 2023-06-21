package com.simprints.id.exceptions.safe.secure

import com.simprints.core.exceptions.SafeException

class ProjectEndingException(message: String = "NotSignedInException") : SafeException(message)
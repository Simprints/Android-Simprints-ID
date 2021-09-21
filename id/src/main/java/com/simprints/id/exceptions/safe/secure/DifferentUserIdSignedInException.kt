package com.simprints.id.exceptions.safe.secure

import com.simprints.core.exceptions.SafeException


class DifferentUserIdSignedInException(message: String = "DifferentUserIdSignedInException") : SafeException(message)

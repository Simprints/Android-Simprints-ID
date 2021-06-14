package com.simprints.id.exceptions.safe

import com.simprints.core.exceptions.SafeException

class MalfunctionException(message: String = "User reported exception"): SafeException(message)

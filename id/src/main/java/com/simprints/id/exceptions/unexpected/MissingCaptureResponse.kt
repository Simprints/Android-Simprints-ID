package com.simprints.id.exceptions.unexpected

import com.simprints.core.exceptions.UnexpectedException

class MissingCaptureResponse(message: String = "No capture response. Must be either fingerprint, face or both") : UnexpectedException(message)

package com.simprints.face.exceptions

import java.lang.RuntimeException

class InvalidFaceRequestException(message: String = ""): RuntimeException(message)
class InvalidFaceResponseException(message: String = ""): RuntimeException(message)

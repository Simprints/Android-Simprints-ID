package com.simprints.face.exceptions

import java.lang.RuntimeException

class InvalidFaceRequestException(message: String = ""): RuntimeException(message)
class FaceUnexpectedException(message: String) : RuntimeException(message)

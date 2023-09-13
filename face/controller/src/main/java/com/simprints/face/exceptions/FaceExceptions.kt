package com.simprints.face.exceptions

class InvalidFaceRequestException(message: String = ""): RuntimeException(message)
class FaceUnexpectedException(message: String) : RuntimeException(message)

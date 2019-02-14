package com.simprints.id.exceptions.unexpected


class GoogleServicesJsonNotFoundError(message: String = "GoogleServicesJsonNotFoundError") : UnexpectedException(message) {

    companion object {

        fun forFile(fileName: String) =
            GoogleServicesJsonNotFoundError("Google Services Json file not found: $fileName")
    }
}

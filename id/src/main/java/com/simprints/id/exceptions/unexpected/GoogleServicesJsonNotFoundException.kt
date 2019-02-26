package com.simprints.id.exceptions.unexpected


class GoogleServicesJsonNotFoundException(message: String = "GoogleServicesJsonNotFoundException") : UnexpectedException(message) {

    companion object {

        fun forFile(fileName: String) =
            GoogleServicesJsonNotFoundException("Google Services Json file not found: $fileName")
    }
}

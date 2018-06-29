package com.simprints.id.exceptions.unsafe


class GoogleServicesJsonNotFoundError(message: String = "GoogleServicesJsonNotFoundError") : SimprintsError(message) {

    companion object {

        fun forFile(fileName: String) =
            GoogleServicesJsonNotFoundError("Google Services Json file not found: $fileName")
    }
}

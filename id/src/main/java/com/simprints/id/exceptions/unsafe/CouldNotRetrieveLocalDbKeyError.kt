package com.simprints.id.exceptions.unsafe


class CouldNotRetrieveLocalDbKeyError(message: String = "CouldNotRetrieveLocalDbKeyError"): SimprintsError(message) {

    companion object {
        fun withException(e: Throwable?) = CouldNotRetrieveLocalDbKeyError(
            "CouldNotRetrieveLocalDbKeyError with exception: \n ${e?.printStackTrace()?: "none"}")
    }
}

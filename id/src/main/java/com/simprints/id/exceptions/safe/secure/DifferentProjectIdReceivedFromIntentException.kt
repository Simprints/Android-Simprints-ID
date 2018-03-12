package com.simprints.id.exceptions.safe.secure


class DifferentProjectIdReceivedFromIntentException(message: String = "DifferentProjectIdReceivedFromIntentException")
    : RuntimeException(message) {

    companion object {
        fun withProjectIds(expected: String, received: String) = DifferentProjectIdReceivedFromIntentException(
            "DifferentProjectIdReceivedFromIntentException: expected = $expected , received = $received")
    }
}

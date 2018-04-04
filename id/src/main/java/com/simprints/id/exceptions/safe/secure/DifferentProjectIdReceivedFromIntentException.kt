package com.simprints.id.exceptions.safe.secure

import com.simprints.id.exceptions.safe.SimprintsException


class DifferentProjectIdReceivedFromIntentException(message: String = "DifferentProjectIdReceivedFromIntentException")
    : SimprintsException(message) {

    companion object {
        fun withProjectIds(expected: String, received: String) = DifferentProjectIdReceivedFromIntentException(
            "DifferentProjectIdReceivedFromIntentException: expected = $expected , received = $received")
    }
}

package com.simprints.clientapi.exceptions

class InvalidIntentActionException(message: String = "Received an intent that SID should not receive") : RuntimeException(message)

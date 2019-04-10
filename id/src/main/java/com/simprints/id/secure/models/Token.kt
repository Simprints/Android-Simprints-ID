package com.simprints.id.secure.models

import java.io.Serializable

data class Token(val legacyToken: String = ""): Serializable //STOPSHIP: Rename legacyToken according to new cloud response

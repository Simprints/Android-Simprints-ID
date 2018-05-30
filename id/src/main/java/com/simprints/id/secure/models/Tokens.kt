package com.simprints.id.secure.models

import java.io.Serializable

data class Tokens(val firestoreToken: String = "", val legacyToken: String = ""): Serializable

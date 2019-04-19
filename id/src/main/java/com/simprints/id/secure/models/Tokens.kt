package com.simprints.id.secure.models

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Tokens(val firestoreToken: String = "", val legacyToken: String = ""): Serializable

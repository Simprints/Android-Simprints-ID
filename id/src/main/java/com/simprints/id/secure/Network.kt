package com.simprints.id.secure

class Network {
    fun execute(success: (String) -> Unit, error: (e: Exception) -> Unit) {
        success("response")
    }
}

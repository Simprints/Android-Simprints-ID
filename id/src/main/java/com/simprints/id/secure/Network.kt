package com.simprints.id.secure

class Network {
    fun execute(success: (String) -> Unit, error: (Exception) -> Unit) {
        if (Math.random() < 0.95) {
            success("response")
        } else {
            error(Exception("Failed to request ApiKey"))
        }
    }
}

package com.simprints.id.secure

//Temporary to "simulate" async network call in our rxjava flow - il will deleted
class Network {
    fun execute(success: (String) -> Unit, error: (e: Exception) -> Unit) {
        success("response")
    }
}

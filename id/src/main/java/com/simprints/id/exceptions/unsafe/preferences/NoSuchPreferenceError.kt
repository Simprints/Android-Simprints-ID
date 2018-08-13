package com.simprints.id.exceptions.unsafe.preferences

import com.simprints.id.exceptions.unsafe.SimprintsError

class NoSuchPreferenceError(message: String = "NoSuchPreferenceError") : SimprintsError(message) {

    companion object {
        fun forKey(key: String) = NoSuchPreferenceError("NoSuchPreferenceError for key: $key")
    }
}

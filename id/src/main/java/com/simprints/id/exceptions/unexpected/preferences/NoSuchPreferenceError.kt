package com.simprints.id.exceptions.unexpected.preferences

import com.simprints.core.exceptions.UnexpectedException

class NoSuchPreferenceError(message: String = "NoSuchPreferenceError") : UnexpectedException(message) {

    companion object {
        fun forKey(key: String) = NoSuchPreferenceError("NoSuchPreferenceError for key: $key")
    }
}

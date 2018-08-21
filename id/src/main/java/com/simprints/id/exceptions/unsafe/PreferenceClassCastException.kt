package com.simprints.id.exceptions.unsafe

class PreferenceClassCastException(message: String = "PreferenceClassCastException") : Error(message) {
    companion object {
        fun withKey(key: String) = PreferenceClassCastException("PreferenceClassCastException with key $key")
    }
}

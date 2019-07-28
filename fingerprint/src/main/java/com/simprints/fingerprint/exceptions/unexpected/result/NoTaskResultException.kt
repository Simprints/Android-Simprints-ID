package com.simprints.fingerprint.exceptions.unexpected.result

import android.content.Intent
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException

class NoTaskResultException(message: String = "NoTaskResultException") : FingerprintUnexpectedException(message) {

    companion object {
        fun inIntent(intent: Intent?) = NoTaskResultException("Expected task result in Intent: $intent")
    }
}

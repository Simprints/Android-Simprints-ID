package com.simprints.clientapi.validators

import android.content.Intent
import com.simprints.libsimprints.Constants
import java.lang.IllegalArgumentException

class EnrollmentValidator(intent: Intent) : CalloutValidator(intent) {

    override fun validateClientRequest() {

        if (intent.getStringExtra(Constants.SIMPRINTS_PROJECT_ID).isNullOrBlank()) {
            throw IllegalArgumentException()
        }

    }

}

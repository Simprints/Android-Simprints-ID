package com.simprints.clientapi.activities.libsimprints

import com.simprints.id.domain.Constants.Companion.SIMPRINTS_IDENTIFY_INTENT
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_REGISTER_INTENT
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_REGISTER_LAST_BIOMETRICS_INTENT
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_SELECT_GUID_INTENT
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_VERIFICATION
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_VERIFY_INTENT
import com.simprints.libsimprints.Constants.SIMPRINTS_REGISTRATION_LAST_BIOMETRICS
import timber.log.Timber

sealed class LibSimprintsAction(val action: String?) {
    object Enrol : LibSimprintsAction(SIMPRINTS_REGISTER_INTENT)
    object Verify : LibSimprintsAction(SIMPRINTS_IDENTIFY_INTENT)
    object Identify : LibSimprintsAction(SIMPRINTS_VERIFICATION)
    object ConfirmIdentity : LibSimprintsAction(SIMPRINTS_REGISTRATION_LAST_BIOMETRICS)
    object EnrolLastBiometrics : LibSimprintsAction(SIMPRINTS_SELECT_GUID_INTENT)

    object Invalid : LibSimprintsAction(null)

    companion object {

        fun buildLibSimprintsAction(action: String?): LibSimprintsAction =
            when (action) {
                SIMPRINTS_REGISTER_INTENT -> Enrol
                SIMPRINTS_IDENTIFY_INTENT -> Identify
                SIMPRINTS_VERIFY_INTENT -> Verify
                SIMPRINTS_REGISTER_LAST_BIOMETRICS_INTENT -> EnrolLastBiometrics
                SIMPRINTS_SELECT_GUID_INTENT -> ConfirmIdentity
                else -> Invalid
            }.also {
                Timber.d("TEST2 $action $it")
            }
    }
}

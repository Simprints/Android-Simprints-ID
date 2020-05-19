package com.simprints.clientapi.activities.libsimprints

import  com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.*
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_IDENTIFY_INTENT
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_REGISTER_INTENT
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_REGISTER_LAST_BIOMETRICS_INTENT
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_SELECT_GUID_INTENT
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_VERIFICATION
import com.simprints.id.domain.Constants.Companion.SIMPRINTS_VERIFY_INTENT
import timber.log.Timber

sealed class LibSimprintsAction(open val action: String?) {

    sealed class LibSimprintsActionFollowUpAction(override val action: String?): LibSimprintsAction(action) {
        object ConfirmIdentity : LibSimprintsActionFollowUpAction(SIMPRINTS_SELECT_GUID_INTENT)
        object EnrolLastBiometrics : LibSimprintsActionFollowUpAction(SIMPRINTS_REGISTER_LAST_BIOMETRICS_INTENT)
    }

    object Enrol : LibSimprintsAction(SIMPRINTS_REGISTER_INTENT)
    object Verify : LibSimprintsAction(SIMPRINTS_IDENTIFY_INTENT)
    object Identify : LibSimprintsAction(SIMPRINTS_VERIFICATION)

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
            }
    }
}

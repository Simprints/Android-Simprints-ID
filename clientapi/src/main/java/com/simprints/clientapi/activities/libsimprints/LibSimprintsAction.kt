package com.simprints.clientapi.activities.libsimprints

import com.simprints.libsimprints.Constants

sealed class LibSimprintsAction {
    object Enrol : LibSimprintsAction()
    object Verify : LibSimprintsAction()
    object Identify : LibSimprintsAction()
    object ConfirmIdentity : LibSimprintsAction()
    object EnrolLastBiometrics : LibSimprintsAction()

    object Invalid : LibSimprintsAction()

    companion object {

        fun buildLibSimprintsAction(action: String?): LibSimprintsAction =
            when (action) {
                Constants.SIMPRINTS_REGISTER_INTENT -> Enrol
                Constants.SIMPRINTS_IDENTIFY_INTENT -> Identify
                Constants.SIMPRINTS_VERIFICATION -> Verify
                Constants.SIMPRINTS_REGISTRATION_LAST_BIOMETRICS -> Verify
                Constants.SIMPRINTS_SELECT_GUID_INTENT -> ConfirmIdentity
                else -> Invalid
            }
    }
}

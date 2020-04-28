package com.simprints.clientapi.activities.libsimprints

import com.simprints.libsimprints.Constants

sealed class LibSimprintsAction {
    object Register : LibSimprintsAction()
    object Verify : LibSimprintsAction()
    object Identify : LibSimprintsAction()
    object ConfirmIdentity : LibSimprintsAction()

    companion object {

        fun buildCommCareAction(action: String?): LibSimprintsAction? =
            when (action) {
                Constants.SIMPRINTS_REGISTER_INTENT -> Register
                Constants.SIMPRINTS_IDENTIFY_INTENT -> Identify
                Constants.SIMPRINTS_VERIFICATION -> Verify
                Constants.SIMPRINTS_SELECT_GUID_INTENT -> ConfirmIdentity
                else -> null
            }
    }
}

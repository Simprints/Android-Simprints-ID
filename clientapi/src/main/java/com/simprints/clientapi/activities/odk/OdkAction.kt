package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.commcare.CommCareAction

sealed class OdkAction {
    object Register : OdkAction()
    object Verify : OdkAction()
    object Identify : OdkAction()
    object ConfirmIdentity : OdkAction()

    companion object {

        private const val PACKAGE_NAME = "com.simprints.simodkadapter"
        private const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        private const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        private const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        private const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"

        fun buildCommCareAction(action: String?): CommCareAction? =
            when (action) {
                ACTION_REGISTER -> CommCareAction.Register
                ACTION_IDENTIFY -> CommCareAction.Identify
                ACTION_VERIFY -> CommCareAction.Verify
                ACTION_CONFIRM_IDENTITY -> CommCareAction.ConfirmIdentity
                else -> null
            }
    }
}

package com.simprints.clientapi.activities.odk

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

        fun buildOdkAction(action: String?): OdkAction? =
            when (action) {
                ACTION_REGISTER -> Register
                ACTION_IDENTIFY -> Identify
                ACTION_VERIFY -> Verify
                ACTION_CONFIRM_IDENTITY -> ConfirmIdentity
                else -> null
            }
    }
}

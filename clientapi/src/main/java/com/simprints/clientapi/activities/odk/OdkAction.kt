package com.simprints.clientapi.activities.odk

sealed class OdkAction {
    object Enrol : OdkAction()
    object Verify : OdkAction()
    object Identify : OdkAction()
    object ConfirmIdentity : OdkAction()
    object EnrolLastBiometrics : OdkAction()

    object Invalid : OdkAction()

    companion object {

        private const val PACKAGE_NAME = "com.simprints.simodkadapter"
        private const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        private const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        private const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        private const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
        private const val ACTION_REGISTER_LAST_BIOMETRICS = "$PACKAGE_NAME.REGISTER_LAST_BIOMETRICS"

        fun buildOdkAction(action: String?): OdkAction =
            when (action) {
                ACTION_REGISTER -> Enrol
                ACTION_IDENTIFY -> Identify
                ACTION_VERIFY -> Verify
                ACTION_CONFIRM_IDENTITY -> ConfirmIdentity
                ACTION_REGISTER_LAST_BIOMETRICS -> EnrolLastBiometrics
                else -> Invalid
            }
    }
}

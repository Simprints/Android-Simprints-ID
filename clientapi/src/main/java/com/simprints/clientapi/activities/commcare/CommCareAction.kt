package com.simprints.clientapi.activities.commcare

sealed class CommCareAction {
    object Register : CommCareAction()
    object Verify : CommCareAction()
    object Identify : CommCareAction()
    object ConfirmIdentity : CommCareAction()

    object Invalid : CommCareAction()

    companion object {
        private const val PACKAGE_NAME = "com.simprints.commcare"
        private const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        private const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        private const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        private const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"

        fun buildCommCareAction(action: String?): CommCareAction =
            when (action) {
                ACTION_REGISTER -> Register
                ACTION_IDENTIFY -> Identify
                ACTION_VERIFY -> Verify
                ACTION_CONFIRM_IDENTITY -> ConfirmIdentity
                else -> Invalid
            }
    }
}

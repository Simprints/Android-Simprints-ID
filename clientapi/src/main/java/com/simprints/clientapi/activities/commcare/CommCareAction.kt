package com.simprints.clientapi.activities.commcare

sealed class CommCareAction(val action: String?) {
    object Enrol : CommCareAction(ACTION_REGISTER)
    object Verify : CommCareAction(ACTION_IDENTIFY)
    object Identify : CommCareAction(ACTION_VERIFY)
    object ConfirmIdentity : CommCareAction(ACTION_CONFIRM_IDENTITY)

    object Invalid : CommCareAction(null)

    companion object {
        private const val PACKAGE_NAME = "com.simprints.commcare"
        private const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        private const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        private const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"

        fun buildCommCareAction(action: String?): CommCareAction =
            when (action) {
                ACTION_REGISTER -> Enrol
                ACTION_IDENTIFY -> Identify
                ACTION_VERIFY -> Verify
                ACTION_CONFIRM_IDENTITY -> ConfirmIdentity
                else -> Invalid
            }
    }
}

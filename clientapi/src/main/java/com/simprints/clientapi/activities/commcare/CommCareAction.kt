package com.simprints.clientapi.activities.commcare
import com.simprints.clientapi.activities.commcare.CommCareAction.CommCareActionFollowUpAction.ConfirmIdentity

sealed class CommCareAction(open val action: String?) {

    sealed class CommCareActionFollowUpAction(override val action: String?): CommCareAction(action) {
        object ConfirmIdentity : CommCareActionFollowUpAction(ACTION_CONFIRM_IDENTITY)
    }

    object Enrol : CommCareAction(ACTION_REGISTER)
    object Verify : CommCareAction(ACTION_IDENTIFY)
    object Identify : CommCareAction(ACTION_VERIFY)

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


package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction.*
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction
import com.simprints.id.domain.Constants

sealed class OdkAction(open val action: String?) {

    sealed class OdkActionFollowUpAction(override val action: String?) : OdkAction(action) {
        object ConfirmIdentity : OdkActionFollowUpAction(ACTION_CONFIRM_IDENTITY)
        object EnrolLastBiometrics : OdkActionFollowUpAction(ACTION_ENROL_LAST_BIOMETRICS)
    }

    object Enrol : OdkAction(ACTION_ENROL)
    object Verify : OdkAction(ACTION_IDENTIFY)
    object Identify : OdkAction(ACTION_VERIFY)

    object Invalid : OdkAction(null)

    companion object {

        private const val PACKAGE_NAME = "com.simprints.simodkadapter"
        private const val ACTION_ENROL = "$PACKAGE_NAME.REGISTER"
        private const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        private const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        private const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
        private const val ACTION_ENROL_LAST_BIOMETRICS = "$PACKAGE_NAME.REGISTER_LAST_BIOMETRICS"

        fun buildOdkAction(action: String?): OdkAction =
            when (action) {
                ACTION_ENROL -> Enrol
                ACTION_IDENTIFY -> Identify
                ACTION_VERIFY -> Verify
                ACTION_CONFIRM_IDENTITY -> ConfirmIdentity
                ACTION_ENROL_LAST_BIOMETRICS -> EnrolLastBiometrics
                else -> Invalid
            }
    }
}

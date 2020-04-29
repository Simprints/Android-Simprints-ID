package com.simprints.clientapi.integration.odk

import android.content.Intent
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.BaseClientApiTest

open class BaseOdkClientApiTest : BaseClientApiTest() {

    private val odkActivityName = OdkActivity::class.qualifiedName!!

    internal val odkConfirmIntentRequest = baseConfirmIntentRequest.apply {
        setClassName(packageName, odkActivityName)
    }

    internal val odkBaseIntentRequest = baseIntentRequest.apply {
        setClassName(packageName, odkActivityName)
    }

    fun makeIntentRequestInvalid(baseIntent: Intent, invalidParam: Pair<String, String> = projectIdField) =
        super.getInvalidIntentRequest(baseIntent, invalidParam).apply {
            setClassName(packageName, odkActivityName)
        }

    override fun makeIntentRequestSuspicious(baseIntent: Intent) =
        super.makeIntentRequestSuspicious(baseIntent).apply {
            setClassName(packageName, odkActivityName)
        }
    

    companion object {
        internal const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        internal const val ODK_GUIDS_KEY = "odk-guids"
        internal const val ODK_BIOMETRICS_COMPLETE_KEY = "odk-biometrics-complete"
        internal const val ODK_CONFIDENCES_KEY = "odk-confidences"
        internal const val ODK_TIERS_KEY = "odk-tiers"
        internal const val ODK_SESSION_ID = "odk-session-id"
        internal const val ODK_EXIT_REASON = "odk-exit-reason"
        internal const val ODK_EXIT_EXTRA = "odk-exit-extra"

        internal const val ODK_ENROL_ACTION = "com.simprints.simodkadapter.REGISTER"
        internal const val ODK_IDENTIFY_ACTION = "com.simprints.simodkadapter.IDENTIFY"
        internal const val ODK_VERIFY_ACTION = "com.simprints.simodkadapter.VERIFY"
        internal const val ODK_CONFIRM_IDENTITY_ACTION = "com.simprints.simodkadapter.CONFIRM_IDENTITY"
    }
}

package com.simprints.clientapi.integration.odk

import android.content.Intent
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.key
import com.simprints.clientapi.integration.value

open class BaseOdkClientApiTest : BaseClientApiTest() {

    private val odkActivityName = OdkActivity::class.qualifiedName!!

    internal val odkConfirmIntentRequest = baseIntentRequest.apply {
        setClassName(packageName, odkActivityName)
        putExtra(sessionIdField.key(), sessionIdField.value())
        putExtra(selectedGuidField.key(), selectedGuidField.value())
    }

    internal val odkBaseFlowIntentRequest = baseIntentRequest.apply {
        setClassName(packageName, odkActivityName)
        putExtra(moduleIdField.key(), moduleIdField.value())
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
        internal const val ODK_GUIDS_KEY = "odk-guids"
        internal const val ODK_BIOMETRICS_COMPLETE_CHECK_KEY = "odk-biometrics-complete"
        internal const val ODK_CONFIDENCES_KEY = "odk-confidences"
        internal const val ODK_TIERS_KEY = "odk-tiers"
        internal const val ODK_EXIT_REASON = "odk-exit-reason"
        internal const val ODK_EXIT_EXTRA = "odk-exit-extra"
        internal const val ODK_SESSION_ID = "odk-session-id"

        internal const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        internal const val ODK_REGISTER_BIOMETRICS_COMPLETE = "odk-register-biometrics-complete"

        internal const val ODK_IDENTIFY_BIOMETRICS_COMPLETE = "odk-identify-biometrics-complete"
        internal const val ODK_MATCH_CONFIDENCE_FLAGS_KEY = "odk-match-confidence-flags"
        internal const val ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY = "odk-highest-match-confidence-flag"

        internal const val ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE = "odk-confirm-identity-biometrics-complete"

        internal const val ODK_VERIFY_BIOMETRICS_COMPLETE = "odk-verify-biometrics-complete"

        internal const val ODK_ENROL_ACTION = "com.simprints.simodkadapter.REGISTER"
        internal const val ODK_ENROL_LAST_BIOMETRICS_ACTION = "com.simprints.simodkadapter.REGISTER_LAST_BIOMETRICS"
        internal const val ODK_IDENTIFY_ACTION = "com.simprints.simodkadapter.IDENTIFY"
        internal const val ODK_VERIFY_ACTION = "com.simprints.simodkadapter.VERIFY"
        internal const val ODK_CONFIRM_IDENTITY_ACTION = "com.simprints.simodkadapter.CONFIRM_IDENTITY"
    }
}

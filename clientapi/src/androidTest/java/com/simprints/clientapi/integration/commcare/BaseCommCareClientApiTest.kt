package com.simprints.clientapi.integration.commcare

import android.content.Intent
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.BaseClientApiTest

open class BaseCommCareClientApiTest : BaseClientApiTest() {

    private val commCareActivityName = CommCareActivity::class.qualifiedName!!

    internal val commCareConfirmIntentRequest = baseConfirmIntentRequest.apply {
        setClassName(packageName, commCareActivityName)
    }

    internal val commCareBaseIntentRequest = baseIntentRequest.apply {
        setClassName(packageName, commCareActivityName)
    }

    fun makeIntentRequestInvalid(baseIntent: Intent, invalidParam: Pair<String, String> = projectIdField) =
        super.getInvalidIntentRequest(baseIntent, invalidParam).apply {
            setClassName(packageName, commCareActivityName)
        }

    override fun makeIntentRequestSuspicious(baseIntent: Intent) =
        super.makeIntentRequestSuspicious(baseIntent).apply {
            setClassName(packageName, commCareActivityName)
        }


    companion object {
        const val COMMCARE_BUNDLE_KEY = "odk_intent_bundle"

        const val BIOMETRICS_COMPLETE_KEY = "biometricsComplete"
        const val REGISTRATION_GUID_KEY = "guid"
        const val VERIFICATION_CONFIDENCE_KEY = "confidence"
        const val VERIFICATION_TIER_KEY = "tier"
        const val VERIFICATION_GUID_KEY = "guid"
        const val EXIT_REASON = "exitReason"
        const val EXIT_EXTRA = "exitExtra"

        internal const val COMMCARE_ENROL_ACTION = "com.simprints.commcare.REGISTER"
        internal const val COMMCARE_IDENTIFY_ACTION = "com.simprints.commcare.IDENTIFY"
        internal const val COMMCARE_VERIFY_ACTION = "com.simprints.commcare.VERIFY"
        internal const val COMMCARE_CONFIRM_IDENTITY_ACTION = "com.simprints.commcare.CONFIRM_IDENTITY"
    }
}

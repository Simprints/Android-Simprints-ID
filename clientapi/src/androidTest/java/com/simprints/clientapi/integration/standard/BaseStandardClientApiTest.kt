package com.simprints.clientapi.integration.standard

import android.content.Intent
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.key
import com.simprints.clientapi.integration.value

open class BaseStandardClientApiTest: BaseClientApiTest() {

    private val standardActivityName = LibSimprintsActivity::class.qualifiedName!!

    internal val standardConfirmIntentRequest = baseIntentRequest.apply {
        setClassName(packageName, standardActivityName)
        putExtra(sessionIdField.key(), sessionIdField.value())
        putExtra(selectedGuidField.key(), selectedGuidField.value())
    }

    internal val standardBaseFlowIntentRequest = baseIntentRequest.apply {
        setClassName(packageName, standardActivityName)
        putExtra(moduleIdField.key(), moduleIdField.value())
    }

    fun makeIntentRequestInvalid(baseIntent: Intent, invalidParam: Pair<String, String> = projectIdField) =
        super.getInvalidIntentRequest(baseIntent, invalidParam).apply {
            setClassName(packageName, standardActivityName)
        }

    override fun makeIntentRequestSuspicious(baseIntent: Intent) =
        super.makeIntentRequestSuspicious(baseIntent).apply {
            setClassName(packageName, standardActivityName)
        }

    companion object {
        internal const val STANDARD_ENROL_ACTION = "com.simprints.id.REGISTER"
        internal const val STANDARD_ENROL_LAST_BIOMETRICS_ACTION = "com.simprints.id.REGISTER_LAST_BIOMETRICS"
        internal const val STANDARD_IDENTIFY_ACTION = "com.simprints.id.IDENTIFY"
        internal const val STANDARD_VERIFY_ACTION = "com.simprints.id.VERIFY"
        internal const val STANDARD_CONFIRM_IDENTITY_ACTION = "com.simprints.id.CONFIRM_IDENTITY"
    }
}

package com.simprints.clientapi.integration.standard

import android.content.Intent
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.BaseClientApiTest

open class BaseStandardClientApiTest: BaseClientApiTest() {

    private val standardActivityName = LibSimprintsActivity::class.qualifiedName!!

    internal val standardConfirmIntentRequest = baseConfirmIntentRequest.apply {
        setClassName(packageName, standardActivityName)
    }

    internal val standardBaseIntentRequest = baseIntentRequest.apply {
        setClassName(packageName, standardActivityName)
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
        internal const val STANDARD_IDENTIFY_ACTION = "com.simprints.id.IDENTIFY"
        internal const val STANDARD_VERIFY_ACTION = "com.simprints.id.VERIFY"
        internal const val STANDARD_CONFIRM_IDENTITY_ACTION = "com.simprints.id.CONFIRM_IDENTITY"
    }
}

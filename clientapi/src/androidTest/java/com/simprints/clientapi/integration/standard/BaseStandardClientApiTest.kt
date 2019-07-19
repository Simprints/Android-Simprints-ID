package com.simprints.clientapi.integration.standard

import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.BaseClientApiTest

open class BaseStandardClientApiTest: BaseClientApiTest() {

    private val standardActivityName = LibSimprintsActivity::class.qualifiedName!!

    internal val standardBaseIntentRequest = baseIntentRequest.apply {
        setClassName(packageName, standardActivityName)
    }

    internal val standardInvalidIntentRequest = invalidIntentRequest.apply {
        setClassName(packageName, standardActivityName)
    }

    internal val standardSuspiciousIntentRequest = suspiciousIntentRequest.apply {
        setClassName(packageName, standardActivityName)
    }

    companion object {
        internal const val STANDARD_ENROL_ACTION = "com.simprints.id.REGISTER"
        internal const val STANDARD_IDENTIFY_ACTION = "com.simprints.id.IDENTIFY"
        internal const val STANDARD_VERIFY_ACTION = "com.simprints.id.VERIFY"
        internal const val STANDARD_CONFIRM_IDENTITY_ACTION = "com.simprints.id.CONFIRM_IDENTITY"
    }
}

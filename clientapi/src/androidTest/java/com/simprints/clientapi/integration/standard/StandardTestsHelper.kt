package com.simprints.clientapi.integration.standard

import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.baseIntentRequest
import com.simprints.clientapi.integration.invalidIntentRequest
import com.simprints.clientapi.integration.packageName
import com.simprints.clientapi.integration.suspiciousIntentRequest

internal const val standardEnrolAction = "com.simprints.id.REGISTER"
internal const val standardIdentifyAction = "com.simprints.id.IDENTIFY"
internal const val standardVerifyAction = "com.simprints.id.VERIFY"
internal const val standardConfirmIdentityAction = "com.simprints.id.CONFIRM_IDENTITY"

internal val standardActivityName = LibSimprintsActivity::class.qualifiedName!!

internal val standardBaseIntentRequest = baseIntentRequest.apply {
    setClassName(packageName, standardActivityName)
}

internal val standardInvalidIntentRequest = invalidIntentRequest.apply {
    setClassName(packageName, standardActivityName)
}

internal val standardSuspiciousIntentRequest = suspiciousIntentRequest.apply {
    setClassName(packageName, standardActivityName)
}


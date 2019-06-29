package com.simprints.clientapi.integration.odk

import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.baseIntentRequest
import com.simprints.clientapi.integration.invalidIntentRequest
import com.simprints.clientapi.integration.packageName
import com.simprints.clientapi.integration.suspiciousIntentRequest

internal const val odkEnrolAction = "com.simprints.simodkadapter.REGISTER"
internal const val odkIdentifyAction = "com.simprints.simodkadapter.IDENTIFY"
internal const val odkVerifyAction = "com.simprints.simodkadapter.VERIFY"
internal const val odkConfirmIdentityAction = "com.simprints.simodkadapter.CONFIRM_IDENTITY"

internal val odkActivityName = OdkActivity::class.qualifiedName!!

internal val odkBaseIntentRequest = baseIntentRequest.apply {
    setClassName(packageName, odkActivityName)
}

internal val odkInvalidIntentRequest = invalidIntentRequest.apply {
    setClassName(packageName, odkActivityName)
}

internal val odkSuspiciousIntentRequest = suspiciousIntentRequest.apply {
    setClassName(packageName, odkActivityName)
}

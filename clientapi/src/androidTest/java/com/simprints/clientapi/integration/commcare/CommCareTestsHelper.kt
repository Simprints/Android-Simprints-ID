package com.simprints.clientapi.integration.commcare

import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.baseIntentRequest
import com.simprints.clientapi.integration.invalidIntentRequest
import com.simprints.clientapi.integration.packageName
import com.simprints.clientapi.integration.suspiciousIntentRequest

internal const val commcareEnrolAction = "com.simprints.commcare.REGISTER"
internal const val commcareIdentifyAction = "com.simprints.commcare.IDENTIFY"
internal const val commcareVerifyAction = "com.simprints.commcare.VERIFY"
internal const val commcareConfirmIdentityAction = "com.simprints.commcare.CONFIRM_IDENTITY"

internal val commCareActivityName = CommCareActivity::class.qualifiedName!!

internal val commCareBaseIntentRequest = baseIntentRequest.apply {
    setClassName(packageName, commCareActivityName)
}

internal val commCareInvalidIntentRequest = invalidIntentRequest.apply {
    setClassName(packageName, commCareActivityName)
}

internal val commCareSuspiciousIntentRequest = suspiciousIntentRequest.apply {
    setClassName(packageName, commCareActivityName)
}

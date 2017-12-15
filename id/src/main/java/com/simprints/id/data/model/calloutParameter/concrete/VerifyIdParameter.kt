package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants

class VerifyIdParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_VERIFY_GUID, "") {

    private val intentAction: String? = intent.action

    override fun validate() {
        val verifyId = value
        if (intentAction.isVerify() && verifyId.isEmpty()) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_VERIFY_GUID)
        }
        if (!intentAction.isVerify() && !verifyId.isEmpty()) {
            throw InvalidCalloutError(ALERT_TYPE.UNEXPECTED_PARAMETER)
        }
    }

    private fun String?.isVerify(): Boolean =
            this == CalloutType.VERIFY.intentAction
}
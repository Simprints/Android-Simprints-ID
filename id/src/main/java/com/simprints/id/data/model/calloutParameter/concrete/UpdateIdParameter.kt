package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.tools.exceptions.InvalidCalloutException
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants

class UpdateIdParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_UPDATE_GUID, "") {

    private val intentAction: String? = intent.action

    override fun validate() {
        val updateId = value
        if (intentAction.isUpdate() && updateId.isEmpty()) {
            throw InvalidCalloutException(ALERT_TYPE.MISSING_UPDATE_GUID)
        }
        if (!intentAction.isUpdate() && !updateId.isEmpty()) {
            throw InvalidCalloutException(ALERT_TYPE.UNEXPECTED_PARAMETER)
        }
    }

    private fun String?.isUpdate(): Boolean =
            this == CalloutType.UPDATE.intentAction
}
package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.GuidValidator
import com.simprints.libsimprints.Constants
import java.util.regex.Pattern


class ApiKeyParameter(intent: Intent,
                      private val guidValidator: GuidValidator = GuidValidator())
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_API_KEY, "") {

    companion object {
        private val GUID_REG_EX = "^([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}$"
        private val GUID_PATTERN = Pattern.compile(GUID_REG_EX)
    }

    override fun validate() {
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_API_KEY)
        }
        if (!isGUID()) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_API_KEY)
        }
    }

    private fun isGUID(): Boolean = guidValidator.isGuid(value)

}

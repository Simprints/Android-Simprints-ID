package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.tools.exceptions.InvalidCalloutException
import com.simprints.id.data.model.calloutParameter.CalloutParameter
import com.simprints.id.model.ALERT_TYPE

class UnexpectedParameters(intent: Intent, expectedKeysInExtras: Collection<String>)
    : CalloutParameter<Map<String, Any>> {

    override val value: Map<String, Any> = intent.extras
                ?.keySet()
                ?.filterNot { expectedKeysInExtras.contains(it) }
                ?.map { Pair(it, intent.extras[it]) }
                ?.toMap()
                ?: mapOf()

    override fun validate() {
        if (value.isNotEmpty()) {
            throw InvalidCalloutException(ALERT_TYPE.UNEXPECTED_PARAMETER)
        }
    }

}
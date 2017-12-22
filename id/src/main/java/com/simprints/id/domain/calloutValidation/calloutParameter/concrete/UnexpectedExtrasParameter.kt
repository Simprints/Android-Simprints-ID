package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import android.os.Bundle
import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE


class UnexpectedExtrasParameter(intent: Intent, expectedExtraKeys: Collection<String>)
    : CalloutParameter<Map<String, Any>> {

    override val value: Map<String, Any> = intent.extras
                .keysNotIn(expectedExtraKeys)
                .map { Pair(it, intent.extras[it]) }
                .toMap()

    private fun Bundle?.keysNotIn(keys: Collection<String>): Collection<String> =
        this?.keySet()?.filterNot { it in keys } ?: listOf()

    override fun validate() {
        validateNoUnexpectedParameters()
    }

    private fun validateNoUnexpectedParameters() {
        if (value.isNotEmpty()) {
            throw InvalidCalloutError(ALERT_TYPE.UNEXPECTED_PARAMETER)
        }
    }

}

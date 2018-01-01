package com.simprints.id.domain.sessionParameters.validators

import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import java.util.regex.Pattern


class GuidValidator(private val alertWhenInvalid: ALERT_TYPE,
                    private val pattern: Pattern = Pattern.compile(GUID_REG_EX))
    : Validator<String>{

    companion object {
        private val GUID_REG_EX = "^([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}$"
    }

    override fun validate(value: String) {
        if (!pattern.matcher(value).matches()) {
            throw InvalidCalloutError(alertWhenInvalid)
        }
    }

}

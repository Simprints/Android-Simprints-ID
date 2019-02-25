package com.simprints.id.session.sessionParameters.validators

import com.simprints.id.exceptions.safe.SafeException
import java.util.regex.Pattern

class GuidValidator(private val errorWhenInvalid: SafeException,
                    private val pattern: Pattern = Pattern.compile(GUID_REG_EX))
    : Validator<String> {

    companion object {
        private const val GUID_REG_EX = "^([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}$"
    }

    override fun validate(value: String) {
        if (value.isNotEmpty() && !pattern.matcher(value).matches()) {
            throw errorWhenInvalid
        }
    }
}

package com.simprints.id.session.sessionParameters.validators

class ModuleIdValidator(private val errorWhenInvalid: Error) : Validator<String> {

    override fun validate(value: String) {

        // We disallow all values with a pipe since it collides with moduleIdOptions serialisation
        if (PROHIBITED_CHARACTERS_FOR_MODULE_ID.any { value.contains(it) }) {
            throw errorWhenInvalid
        }
    }

    companion object {
        private val PROHIBITED_CHARACTERS_FOR_MODULE_ID = setOf('|')
    }
}

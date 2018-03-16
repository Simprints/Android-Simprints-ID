package com.simprints.id.session.sessionParameters.validators

import com.google.gson.Gson


class MetadataValidator(private val errorWhenInvalid: Error,
                        private val gson: Gson) : Validator<String>{

    override fun validate(value: String) {
        if (value.isNotEmpty()) {
            try {
                gson.fromJson(value, Any::class.java)
            } catch (ex: com.google.gson.JsonSyntaxException) {
                throw errorWhenInvalid
            }
        }
    }

}

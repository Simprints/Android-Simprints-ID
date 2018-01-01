package com.simprints.id.domain.sessionParameters.validators

import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Metadata


class MetadataValidator(private val alertWhenInvalid: ALERT_TYPE) : Validator<String>{

    override fun validate(value: String) {
        if (value.isNotEmpty()) {
            try {
                Metadata(value)
            } catch (e: Metadata.InvalidMetadataException) {
                throw InvalidCalloutError(alertWhenInvalid)
            }
        }
    }

}

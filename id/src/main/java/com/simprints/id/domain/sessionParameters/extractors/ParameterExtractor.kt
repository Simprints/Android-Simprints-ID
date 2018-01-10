package com.simprints.id.domain.sessionParameters.extractors

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.sessionParameters.readers.Reader
import com.simprints.id.domain.sessionParameters.validators.Validator

class ParameterExtractor<out T: Any>(private val reader: Reader<T>,
                                     private val validator: Validator<T>)
    : Extractor<T> {

    override fun extractFrom(callout: Callout): T =
        reader.readFrom(callout)
        .apply {
            validator.validate(this)
        }

}

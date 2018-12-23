package com.simprints.id.session.sessionParameters.extractors

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.sessionParameters.readers.Reader
import com.simprints.id.session.sessionParameters.validators.Validator

class ParameterExtractor<out T : Any>(private val reader: Reader<T>,
                                      private val validator: Validator<T>) : Extractor<T> {

    override fun extractFrom(callout: Callout): T =
        reader.readFrom(callout)
            .apply {
                validator.validate(this)
            }

}

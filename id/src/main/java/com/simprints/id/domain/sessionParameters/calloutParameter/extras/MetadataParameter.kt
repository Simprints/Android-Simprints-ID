package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Metadata


class MetadataParameter(calloutParameters: CalloutParameters,
                        private val isMetadataValid: (metadata: String) -> Boolean = isMetadataValidWithLibSimprints,
                        defaultValue: String = "")
    : CalloutExtraParameter<String>(calloutParameters, Constants.SIMPRINTS_METADATA, defaultValue) {

    companion object {
        private val isMetadataValidWithLibSimprints = { metadata: String ->
            try {
                Metadata(metadata)
                true
            } catch (e: Metadata.InvalidMetadataException) {
                false
            }
        }
    }

    override fun validate() {
        if (!isMissing) {
            validateValueIsValidMetadata()
        }
    }

    private fun validateValueIsValidMetadata() {
        if (!isMetadataValid(value)) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_METADATA)
        }
    }

}

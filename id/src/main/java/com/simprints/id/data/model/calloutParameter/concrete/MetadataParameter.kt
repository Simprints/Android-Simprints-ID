package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Metadata


class MetadataParameter(intent: Intent,
                        private val isMetadataValid: (metadata: String) -> Boolean = isMetadataValidWithLibSimprints,
                        defaultValue: String = "")
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_METADATA, defaultValue) {

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

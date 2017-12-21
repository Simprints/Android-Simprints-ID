package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Metadata


class MetadataParameter(intent: Intent,
                        private val isMetadataValid: (metadata: String) -> Boolean
                        = isMetadataValidWithLibSimprints)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_METADATA, "") {

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
        val metadata = value
        if (isMissing) {
            return
        }
        if (!isMetadataValid(metadata)) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_METADATA)
        }
    }

}

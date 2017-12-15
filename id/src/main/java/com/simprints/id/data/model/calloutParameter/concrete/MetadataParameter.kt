package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Metadata

class MetadataParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_UPDATE_GUID, "") {

    override fun validate() {
        val metadata = value
        try {
            Metadata(metadata)
        } catch (e: Metadata.InvalidMetadataException) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_METADATA)
        }
    }

}
package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
enum class ApiCallbackType {
    @JsonProperty("Enrolment") ENROLMENT,
    @JsonProperty("Identification") IDENTIFICATION,
    @JsonProperty("Refusal") REFUSAL,
    @JsonProperty("Verification") VERIFICATION,
    @JsonProperty("Error") ERROR,
    @JsonProperty("Confirmation") CONFIRMATION
}

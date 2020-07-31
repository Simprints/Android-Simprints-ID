package com.simprints.id.data.db.event.remote.models.callout

import com.fasterxml.jackson.annotation.JsonProperty
import io.realm.internal.Keep

@Keep
enum class ApiCalloutType {
    @JsonProperty("Confirmation") CONFIRMATION,
    @JsonProperty("Enrolment") ENROLMENT,
    @JsonProperty("EnrolmentLastBiometrics") ENROLMENT_LAST_BIOMETRICS,
    @JsonProperty("Identification") IDENTIFICATION,
    @JsonProperty("Verification") VERIFICATION
}

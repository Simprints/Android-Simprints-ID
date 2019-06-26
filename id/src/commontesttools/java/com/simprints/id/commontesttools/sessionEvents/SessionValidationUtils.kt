package com.simprints.id.commontesttools.sessionEvents

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiAlertScreenEvent
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiArtificialTerminationEvent
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiAuthenticationEvent
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiRefusalEvent
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallbackType
import com.simprints.id.data.analytics.eventdata.models.remote.events.callout.ApiCalloutType
import com.simprints.id.tools.extensions.getString
import com.simprints.id.tools.extensions.isGuid

fun validateAlertScreenEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ALERT_SCREEN")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("alertType").asString).isIn(ApiAlertScreenEvent.ApiAlertScreenEvent.values().valuesAsStrings())
    assertThat(json.size()).isEqualTo(3)
}

fun validateArtificialTerminationEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ARTIFICIAL_TERMINATION")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("reason").asString).isIn(ApiArtificialTerminationEvent.ApiReason.values().valuesAsStrings())
    assertThat(json.size()).isEqualTo(3)
}

fun validateIntentParsingEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("INTENT_PARSING")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("integration").asString).isIn(listOf("STANDARD", "ODK"))
    assertThat(json.size()).isEqualTo(3)
}

fun validateAuthenticationEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("AUTHENTICATION")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    with(json.get("userInfo").asJsonObject) {
        assertThat(getString("projectId")).isNotEmpty()
        assertThat(getString("userId")).isNotEmpty()
        assertThat(size()).isEqualTo(2)
    }
    assertThat(json.get("result").asString).isIn(ApiAuthenticationEvent.ApiResult.values().valuesAsStrings())
    assertThat(json.size()).isEqualTo(5)
}

fun validateCallbackEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("CALLBACK")
    assertThat(json.get("relativeStartTime").asString)
    (json.get("callback").asJsonObject).let {
        val type = ApiCallbackType.valueOf(it.get("type").asString)
        when (type) {
            ApiCallbackType.ENROLMENT -> verifyCallbackEnrolmentApiModel(it)
            ApiCallbackType.IDENTIFICATION -> verifyCallbackIdentificationApiModel(it)
            ApiCallbackType.VERIFICATION -> verifyCallbackVerificationApiModel(it)
            ApiCallbackType.REFUSAL -> verifyCallbackRefusalApiModel(it)
            ApiCallbackType.ERROR -> verifyCallbackErrorApiModel(it)
        }
    }

    assertThat(json.size()).isEqualTo(3)
}

fun verifyCallbackEnrolmentApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ENROLMENT")
    assertThat(json.get("guid").asString)
}

fun verifyCallbackIdentificationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("IDENTIFICATION")
    assertThat(json.get("sessionId").asString)
    verifyCallbackIdentificationScoresApiModel(json.getAsJsonArray("scores"))
}

fun verifyCallbackIdentificationScoresApiModel(jsonArray: JsonArray) {
    assertThat(jsonArray.get(0).asJsonObject.get("guid").asString)
    assertThat(jsonArray.get(0).asJsonObject.get("confidence").asString)
    assertThat(jsonArray.get(0).asJsonObject.get("tier").asString)
}

fun verifyCallbackVerificationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("VERIFICATION")
    with(json.get("score").asJsonObject) {
        assertThat(get("guid").asString)
        assertThat(get("confidence").asString)
    assertThat(get("tier").asString)
    }
}

fun verifyCallbackRefusalApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("REFUSAL")
    assertThat(json.get("reason").asString).isAnyOf("REFUSED_RELIGION", "REFUSED_DATA_CONCERNS", "REFUSED_PERMISSION", "SCANNER_NOT_WORKING", "REFUSED_YOUNG", "REFUSED_SICK", "REFUSED_PREGNANT", "OTHER")
    assertThat(json.get("extra").asString)
}

fun verifyCallbackErrorApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ERROR")
    assertThat(json.get("reason").asString)
}

fun validateCalloutEventApiModel(json: JsonObject) {

    assertThat(json.get("type").asString).isEqualTo("CALLOUT")
    assertThat(json.get("relativeStartTime").asString)
    with(json.get("callout").asJsonObject) {
        when (ApiCalloutType.valueOf(this.get("type").asString)) {
            ApiCalloutType.CONFIRMATION -> verifyCalloutConfirmationApiModel(this)
            ApiCalloutType.ENROLMENT -> verifyCalloutEnrolmentApiModel(this)
            ApiCalloutType.IDENTIFICATION -> verifyCalloutIdentificationApiModel(this)
            ApiCalloutType.VERIFICATION -> verifyCalloutVerificationApiModel(this)
        }
    }
    assertThat(json.size()).isEqualTo(3)
}

fun verifyCalloutVerificationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("VERIFICATION")
    assertThat(json.get("projectId").asString)
    assertThat(json.get("userId").asString)
    assertThat(json.get("moduleId").asString)
    assertThat(json.get("verifyGuid").asString)
    assertThat(json.get("metadata").asString)
    assertThat(json.size()).isEqualTo(6)
}

fun verifyCalloutIdentificationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("IDENTIFICATION")
    assertThat(json.get("projectId").asString)
    assertThat(json.get("userId").asString)
    assertThat(json.get("moduleId").asString)
    assertThat(json.get("metadata").asString)
    assertThat(json.size()).isEqualTo(5)
}

fun verifyCalloutEnrolmentApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ENROLMENT")
    assertThat(json.get("projectId").asString)
    assertThat(json.get("userId").asString)
    assertThat(json.get("moduleId").asString)
    assertThat(json.get("metadata").asString)
    assertThat(json.size()).isEqualTo(5)
}

fun verifyCalloutConfirmationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("CONFIRMATION")
    assertThat(json.get("selectedGuid").asString)
    assertThat(json.get("sessionId").asString)
    assertThat(json.size()).isEqualTo(3)
}

fun validateAuthorizationEventApiModel(json: JsonObject) {

    assertThat(json.get("type").asString).isEqualTo("AUTHORIZATION")
    assertThat(json.get("relativeStartTime").asLong)
    with(json.get("userInfo").asJsonObject) {
        assertThat(getString("projectId")).isNotEmpty()
        assertThat(getString("userId")).isNotEmpty()
        assertThat(size()).isEqualTo(2)
    }
    assertThat(json.get("result").asString).isAnyOf("AUTHORIZED", "NOT_AUTHORIZED")
    assertThat(json.size()).isEqualTo(4)
}

fun validateCandidateReadEventApiModel(json: JsonObject) {

    assertThat(json.get("type").asString).isEqualTo("CANDIDATE_READ")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    assertThat(json.get("candidateId").asString.isGuid()).isTrue()
    assertThat(json.get("localResult").asString).isAnyOf("FOUND", "NOT_FOUND")
    if (json.has("remoteResult")) {
        assertThat(json.get("remoteResult").asString).isAnyOf("FOUND", "NOT_FOUND")
        assertThat(json.size()).isEqualTo(6)
    } else {
        assertThat(json.size()).isEqualTo(5)
    }
}

fun validateConnectivitySnapshotEventApiModel(json: JsonObject) {

    assertThat(json.get("type").asString).isEqualTo("CONNECTIVITY_SNAPSHOT")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("networkType").asString)
    val connections = json.get("connections").asJsonArray
    connections.forEach {
        val connJson = it.asJsonObject
        assertThat(connJson.get("type").asString).isNotEmpty()
        assertThat(connJson.get("state").asString).isNotEmpty()
        assertThat(connJson.size()).isEqualTo(2)
    }
    assertThat(json.size()).isEqualTo(4)
}

fun validateConsentEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("CONSENT")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    assertThat(json.get("consentType").asString).isAnyOf("INDIVIDUAL", "PARENTAL")
    assertThat(json.get("result").asString).isAnyOf("ACCEPTED", "DECLINED", "NO_RESPONSE")
    assertThat(json.size()).isEqualTo(5)
}

fun validateEnrolmentEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ENROLMENT")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("personId").asString.isGuid()).isTrue()
    assertThat(json.size()).isEqualTo(3)
}

fun validateFingerprintCaptureEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("FINGERPRINT_CAPTURE")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    assertThat(json.get("id").asString)
    assertThat(json.get("finger").asString).isAnyOf("LEFT_THUMB", "LEFT_INDEX_FINGER", "LEFT_3RD_FINGER", "LEFT_4TH_FINGER", "LEFT_5TH_FINGER", "RIGHT_THUMB", "RIGHT_INDEX_FINGER", "RIGHT_3RD_FINGER", "RIGHT_4TH_FINGER", "RIGHT_5TH_FINGER")
    assertThat(json.get("qualityThreshold").asNumber)
    assertThat(json.get("result").asString).isAnyOf("GOOD_SCAN", "BAD_QUALITY", "NO_FINGER_DETECTED", "SKIPPED", "FAILURE_TO_ACQUIRE")

    with(json.get("fingerprint").asJsonObject) {
        assertThat(get("finger").asString).isAnyOf("LEFT_THUMB", "LEFT_INDEX_FINGER", "LEFT_3RD_FINGER", "LEFT_4TH_FINGER", "LEFT_5TH_FINGER", "RIGHT_THUMB", "RIGHT_INDEX_FINGER", "RIGHT_3RD_FINGER", "RIGHT_4TH_FINGER", "RIGHT_5TH_FINGER")
        assertThat(get("quality").asInt)
        assertThat(get("template").asString).isNotEmpty()
        assertThat(size()).isEqualTo(3)
    }
    assertThat(json.size()).isEqualTo(8)
}

fun validateGuidSelectionEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("GUID_SELECTION")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("selectedId").asString.isGuid()).isTrue()
    assertThat(json.size()).isEqualTo(3)
}

fun validateMatchEntryApiModel(json: JsonObject) {
    assertThat(json.get("candidateId").asString.isGuid()).isTrue()
    assertThat(json.get("score").asNumber)
    assertThat(json.size()).isEqualTo(2)
}

fun validateOneToManyMatchEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ONE_TO_MANY_MATCH")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    with(json.get("pool").asJsonObject) {
        assertThat(get("type").asString).isAnyOf("PROJECT", "MODULE", "USER")
        assertThat(get("count").asInt)
        assertThat(size()).isEqualTo(2)
    }
    val matchEntries = json.get("result").asJsonArray
    matchEntries.forEach {
        validateMatchEntryApiModel(it.asJsonObject)
    }
    assertThat(json.size()).isEqualTo(5)
}

fun validateOneToOneMatchEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ONE_TO_ONE_MATCH")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    assertThat(json.get("candidateId").asString.isGuid()).isTrue()
    with(json.get("result").asJsonObject) {
        validateMatchEntryApiModel(this)
    }
    assertThat(json.size()).isEqualTo(5)
}

fun validatePersonCreationEvent(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("PERSON_CREATION")
    assertThat(json.get("relativeStartTime").asLong)
    val fingerprintCaptureIds = json.get("fingerprintCaptureIds").asJsonArray
    fingerprintCaptureIds.forEach {
        assertThat(it.asString.isGuid()).isTrue()
    }
    assertThat(json.size()).isEqualTo(3)
}

fun validateRefusalEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("REFUSAL")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    assertThat(json.get("reason").asString).isIn(ApiRefusalEvent.ApiAnswer.values().valuesAsStrings())
    assertThat(json.get("otherText").asString)
    assertThat(json.size()).isEqualTo(5)
}

fun validateScannerConnectionEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("SCANNER_NOT_WORKING")
    assertThat(json.get("relativeStartTime").asLong)
    with(json.get("scannerInfo").asJsonObject) {
        assertThat(get("lastScannerId").asString).isNotEmpty()
        assertThat(get("macAddress").asString).isNotEmpty()
        assertThat(get("hardwareVersion").asString).isNotEmpty()
    }
    assertThat(json.size()).isEqualTo(3)
}

fun validateEvent(json: JsonObject) {
    val type = json.get("type").asString

    when (EventType.valueOf(type)) {
        EventType.REFUSAL -> validateRefusalEventApiModel(json)
        EventType.CONSENT -> validateConsentEventApiModel(json)
        EventType.ENROLMENT -> validateEnrolmentEventApiModel(json)
        EventType.ALERT_SCREEN -> validateAlertScreenEventApiModel(json)
        EventType.CANDIDATE_READ -> validateCandidateReadEventApiModel(json)
        EventType.AUTHORIZATION -> validateAuthorizationEventApiModel(json)
        EventType.GUID_SELECTION -> validateGuidSelectionEventApiModel(json)
        EventType.AUTHENTICATION -> validateAuthenticationEventApiModel(json)
        EventType.ONE_TO_ONE_MATCH -> validateOneToManyMatchEventApiModel(json)
        EventType.PERSON_CREATION -> validatePersonCreationEvent(json)
        EventType.ONE_TO_MANY_MATCH -> validateOneToManyMatchEventApiModel(json)
        EventType.SCANNER_CONNECTION -> validateScannerConnectionEventApiModel(json)
        EventType.FINGERPRINT_CAPTURE -> validateFingerprintCaptureEventApiModel(json)
        EventType.CONNECTIVITY_SNAPSHOT -> validateConnectivitySnapshotEventApiModel(json)
        EventType.ARTIFICIAL_TERMINATION -> validateArtificialTerminationEventApiModel(json)
        EventType.INVALID_INTENT -> validateInvalidEventApiModel(json)
        EventType.SUSPICIOUS_INTENT -> validateSuspiciousIntentEventApiModel(json)
        EventType.CALLBACK_REFUSAL,
        EventType.CALLBACK_ENROLMENT,
        EventType.CALLBACK_IDENTIFICATION,
        EventType.CALLBACK_VERIFICATION -> validateCallbackEventApiModel(json)
        EventType.CALLOUT_ENROLMENT,
        EventType.CALLOUT_CONFIRMATION,
        EventType.CALLOUT_VERIFICATION,
        EventType.CALLOUT_IDENTIFICATION -> validateCalloutEventApiModel(json)
    }
}

fun validateDatabaseInfoApiModel(json: JsonObject) {
    assertThat(json.get("recordCount").asInt)
    assertThat(json.get("sessionCount").asInt)
    assertThat(json.size()).isEqualTo(2)
}

fun validateDeviceApiModel(json: JsonObject) {
    assertThat(json.get("androidSdkVersion").asString).isNotEmpty()
    assertThat(json.get("deviceModel").asString).isNotEmpty()
    assertThat(json.get("deviceId").asString).isNotEmpty()
    assertThat(json.size()).isEqualTo(3)
}

fun validateLocationApiModel(json: JsonObject) {
    assertThat(json.get("latitude").asFloat)
    assertThat(json.get("longitude").asFloat)
    assertThat(json.size()).isEqualTo(2)
}

fun validateSuspiciousIntentEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("SUSPICIOUS_INTENT")
    assertThat(json.get("relativeStartTime").asLong).isNotNull()
    assertThat(json.get("unexpectedExtras").asJsonObject.toString()).isNotNull()
    assertThat(json.size()).isEqualTo(3)
}

fun validateInvalidEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("INVALID_INTENT")
    assertThat(json.get("relativeStartTime").asLong).isNotNull()
    assertThat(json.get("extras").asJsonObject.toString()).isNotNull()
    assertThat(json.get("action").asString).isNotNull()
    assertThat(json.size()).isEqualTo(4)
}

fun validateSessionEventsApiModel(json: JsonObject) {
    var countFields = 9
    assertThat(json.get("id").asString).isNotEmpty()
    assertThat(json.get("startTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    assertThat(json.get("relativeUploadTime").asString).isNotEmpty()
    assertThat(json.get("appVersionName").asString).isNotEmpty()
    assertThat(json.get("libVersionName").asString).isNotEmpty()

    if (json.has("analyticsId")) {
        json.get("analyticsId").asString
        countFields += 1
    }

    assertThat(json.get("language").asString).isIn(listOf("en", "ne", "bn", "ps", "fa-rAF", "so", "ha", "ny", "ty", "wal"))
    with(json.get("device").asJsonObject) { validateDeviceApiModel(this) }

    if (json.has("databaseInfo")) {
        with(json.get("databaseInfo").asJsonObject) { validateDatabaseInfoApiModel(this) }
        countFields += 1
    }

    if (json.has("location")) {
        with(json.get("location").asJsonObject) { validateLocationApiModel(this) }
        countFields += 1
    }

    val events = json.get("events").asJsonArray
    events.forEach { validateEvent(it.asJsonObject) }

    assertThat(json.size()).isEqualTo(countFields)
}

private fun <T> Array<T>.valuesAsStrings(): List<String> = this.map { it.toString() }

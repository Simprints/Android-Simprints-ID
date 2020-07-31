package com.simprints.id.commontesttools.sessionEvents

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.simprints.id.data.db.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType
import com.simprints.id.data.db.event.remote.models.ApiArtificialTerminationPayload.ApiReason
import com.simprints.id.data.db.event.remote.models.ApiAuthenticationPayload
import com.simprints.id.data.db.event.remote.models.ApiRefusalPayload
import com.simprints.id.data.db.event.remote.models.callback.ApiCallbackType
import com.simprints.id.data.db.event.remote.models.callout.ApiCalloutType
import com.simprints.id.tools.extensions.getString
import com.simprints.id.tools.extensions.isGuid
import com.simprints.testtools.common.syntax.failTest

fun validateCommonParams(json: JsonObject, type: String) {
    with(json.get("labels").asJsonObject) {
        assertThat(get("sessionId").asJsonArray.size()).isEqualTo(1)
        assertThat(get("deviceId").asJsonArray.size()).isEqualTo(1)
        assertThat(get("projectId").asJsonArray.size()).isEqualTo(1)
        assertThat(json.size()).isEqualTo(3)
    }
    with(json.get("payload")) {
        assertThat(getString("type")).isEqualTo(type)
        assertThat(get("version").asInt).isEqualTo(0)
        assertThat(get("relativeStartTime").asString)
    }
    assertThat(json.size()).isEqualTo(4)
}


fun validateCallbackEventApiModel(json: JsonObject) {
    validateCommonParams(json, "Callback")
    with(json.get("payload").asJsonObject) {
        (get("callback").asJsonObject).let {
            val type = ApiCallbackType.valueOf(it.get("type").asString)
            when (type) {
                ApiCallbackType.ENROLMENT -> verifyCallbackEnrolmentApiModel(it)
                ApiCallbackType.IDENTIFICATION -> verifyCallbackIdentificationApiModel(it)
                ApiCallbackType.VERIFICATION -> verifyCallbackVerificationApiModel(it)
                ApiCallbackType.REFUSAL -> verifyCallbackRefusalApiModel(it)
                ApiCallbackType.CONFIRMATION -> verifyCallbackConfirmationApiModel(it)
                ApiCallbackType.ERROR -> verifyCallbackErrorApiModel(it)
            }
        }
        assertThat(json.size()).isEqualTo(3)
    }
}

fun verifyCallbackEnrolmentApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Enrolment")
    assertThat(json.get("guid").asString)
    assertThat(json.size()).isEqualTo(2)
}

fun verifyCallbackIdentificationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Identification")
    assertThat(json.get("sessionId").asString)
    verifyCallbackIdentificationScoresApiModel(json.getAsJsonArray("scores"))
    assertThat(json.size()).isEqualTo(3)
}

fun verifyCallbackIdentificationScoresApiModel(jsonArray: JsonArray) {
    assertThat(jsonArray.get(0).asJsonObject.get("guid").asString)
    assertThat(jsonArray.get(0).asJsonObject.get("confidence").asString)
    assertThat(jsonArray.get(0).asJsonObject.get("tier").asString)
    assertThat(jsonArray.size()).isEqualTo(3)
}

fun verifyCallbackVerificationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Verification")
    with(json.get("score").asJsonObject) {
        assertThat(get("guid").asString)
        assertThat(get("confidence").asString)
        assertThat(get("tier").asString)
        assertThat(size()).isEqualTo(3)
    }
    assertThat(json.size()).isEqualTo(2)
}

fun verifyCallbackRefusalApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Refusal")
    assertThat(json.get("reason").asString).isAnyOf("REFUSED_RELIGION", "REFUSED_DATA_CONCERNS", "REFUSED_PERMISSION", "SCANNER_NOT_WORKING", "REFUSED_NOT_PRESENT", "REFUSED_YOUNG", "OTHER")
    assertThat(json.get("extra").asString)
    assertThat(json.size()).isEqualTo(3)
}

fun verifyCallbackConfirmationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Confirmation")
    assertThat(json.get("received").asBoolean)
    assertThat(json.size()).isEqualTo(2)
}

fun verifyCallbackErrorApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Error")
    assertThat(json.get("reason").asString)
    assertThat(json.size()).isEqualTo(2)
}

fun validateCalloutEventApiModel(json: JsonObject) {
    validateCommonParams(json, "Callout")
    with(json.get("payload").asJsonObject) {
        (get("callback").asJsonObject).let {
            val type = ApiCalloutType.valueOf(it.get("type").asString)
            when (type) {
                ApiCalloutType.CONFIRMATION -> verifyCalloutConfirmationApiModel(this)
                ApiCalloutType.ENROLMENT -> verifyCalloutEnrolmentApiModel(this)
                ApiCalloutType.IDENTIFICATION -> verifyCalloutIdentificationApiModel(this)
                ApiCalloutType.VERIFICATION -> verifyCalloutVerificationApiModel(this)
                ApiCalloutType.ENROLMENT_LAST_BIOMETRICS -> verifyCalloutLastEnrolmentBiometricsApiModel(this)
            }
        }
        assertThat(json.size()).isEqualTo(3)
    }
}

fun verifyCalloutLastEnrolmentBiometricsApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("EnrolmentLastBiometrics")
    assertThat(json.get("projectId").asString)
    assertThat(json.get("userId").asString)
    assertThat(json.get("moduleId").asString)
    assertThat(json.get("metadata").asString)
    assertThat(json.get("sessionId").asString)
    assertThat(json.size()).isEqualTo(6)
}

fun verifyCalloutVerificationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Verification")
    assertThat(json.get("projectId").asString)
    assertThat(json.get("userId").asString)
    assertThat(json.get("moduleId").asString)
    assertThat(json.get("verifyGuid").asString)
    assertThat(json.get("metadata").asString)
    assertThat(json.size()).isEqualTo(6)
}

fun verifyCalloutIdentificationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Identification")
    assertThat(json.get("projectId").asString)
    assertThat(json.get("userId").asString)
    assertThat(json.get("moduleId").asString)
    assertThat(json.get("metadata").asString)
    assertThat(json.size()).isEqualTo(5)
}

fun verifyCalloutEnrolmentApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Enrolment")
    assertThat(json.get("projectId").asString)
    assertThat(json.get("userId").asString)
    assertThat(json.get("moduleId").asString)
    assertThat(json.get("metadata").asString)
    assertThat(json.size()).isEqualTo(5)
}

fun verifyCalloutConfirmationApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("Confirmation")
    assertThat(json.get("selectedGuid").asString)
    assertThat(json.get("sessionId").asString)
    assertThat(json.size()).isEqualTo(2)
}

fun validateAlertScreenEventApiModel(json: JsonObject) {
    validateCommonParams(json, "AlertScreen")
    with(json.get("payload").asJsonObject) {
        assertThat(getString("alertType")).isIn(ApiAlertScreenEventType.values().valuesAsStrings())
        assertThat(size()).isEqualTo(3)
    }
}

fun validateArtificialTerminationEventApiModel(json: JsonObject) {
    validateCommonParams(json, "ArtificialTermination")
    with(json.get("payload").asJsonObject) {
        assertThat(get("reason").asString).isIn(ApiReason.values().valuesAsStrings())
        assertThat(size()).isEqualTo(3)
    }
}

fun validateAuthenticationEventApiModel(json: JsonObject) {
    validateCommonParams(json, "Authentication")
    with(json.get("payload").asJsonObject) {
        assertThat(get("relativeEndTime").asLong)
        with(get("userInfo").asJsonObject) {
            assertThat(getString("projectId")).isNotEmpty()
            assertThat(getString("userId")).isNotEmpty()
            assertThat(size()).isEqualTo(2)
        }
        assertThat(get("result").asString).isIn(ApiAuthenticationPayload.ApiResult.values().valuesAsStrings())
        assertThat(size()).isEqualTo(5)
    }
}

fun validateAuthorizationEventApiModel(json: JsonObject) {
    validateCommonParams(json, "Authorization")

    with(json.get("payload").asJsonObject) {
        with(get("userInfo").asJsonObject) {
            assertThat(getString("projectId")).isNotEmpty()
            assertThat(getString("userId")).isNotEmpty()
            assertThat(size()).isEqualTo(2)
        }
        assertThat(get("result").asString).isAnyOf("AUTHORIZED", "NOT_AUTHORIZED")
        assertThat(size()).isEqualTo(4)
    }
}

fun validateCandidateReadEventApiModel(json: JsonObject) {
    validateCommonParams(json, "CandidateRead")

    with(json.get("payload").asJsonObject) {
        assertThat(get("relativeEndTime").asLong)
        assertThat(get("candidateId").asString.isGuid()).isTrue()
        assertThat(get("localResult").asString).isAnyOf("FOUND", "NOT_FOUND")
        if (has("remoteResult")) {
            assertThat(get("remoteResult").asString).isAnyOf("FOUND", "NOT_FOUND")
            assertThat(size()).isEqualTo(6)
        } else {
            assertThat(size()).isEqualTo(5)
        }
    }
}

fun validateCompletionCheckEventApiModel(json: JsonObject) {
    validateCommonParams(json, "CompletionCheck")

    with(json.get("payload").asJsonObject) {
        assertThat(get("completed").asBoolean)
        assertThat(size()).isEqualTo(1)
    }
}

fun validateConnectivitySnapshotEventApiModel(json: JsonObject) {
    validateCommonParams(json, "ConnectivitySnapshot")
    with(json.get("payload").asJsonObject) {
        assertThat(get("networkType").asString)
        val connections = get("connections").asJsonArray
        connections.forEach {
            val connJson = it.asJsonObject
            assertThat(connJson.get("type").asString).isNotEmpty()
            assertThat(connJson.get("state").asString).isNotEmpty()
            assertThat(connJson.size()).isEqualTo(2)
        }
        assertThat(json.size()).isEqualTo(4)
    }
}


fun validateConsentEventApiModel(json: JsonObject) {
    validateCommonParams(json, "Consent")
    with(json.get("payload").asJsonObject) {
        assertThat(json.get("relativeEndTime").asLong)
        assertThat(json.get("consentType").asString).isAnyOf("INDIVIDUAL", "PARENTAL")
        assertThat(json.get("result").asString).isAnyOf("ACCEPTED", "DECLINED", "NO_RESPONSE")
        assertThat(json.size()).isEqualTo(5)
    }

















fun validateIntentParsingEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("INTENT_PARSING")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.getString("integration")).isIn(listOf("STANDARD", "ODK"))
    assertThat(json.size()).isEqualTo(3)
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
    assertThat(json.get("matcher").asString).isAnyOf("SIM_AFIS", "RANK_ONE")
    with(json.get("pool").asJsonObject) {
        assertThat(get("type").asString).isAnyOf("PROJECT", "MODULE", "USER")
        assertThat(get("count").asInt)
        assertThat(size()).isEqualTo(2)
    }
    val matchEntries = json.get("result").asJsonArray
    matchEntries.forEach {
        validateMatchEntryApiModel(it.asJsonObject)
    }
    assertThat(json.size()).isEqualTo(6)
}

fun validateOneToOneMatchEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("ONE_TO_ONE_MATCH")
    assertThat(json.get("relativeStartTime").asLong)
    assertThat(json.get("relativeEndTime").asLong)
    assertThat(json.get("candidateId").asString.isGuid()).isTrue()
    assertThat(json.get("matcher").asString).isAnyOf("SIM_AFIS", "RANK_ONE")
    with(json.get("result").asJsonObject) {
        validateMatchEntryApiModel(this)
    }
    assertThat(json.size()).isEqualTo(6)
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
    assertThat(json.get("reason").asString).isIn(ApiRefusalPayload.ApiAnswer.values().valuesAsStrings())
    assertThat(json.get("otherText").asString)
    assertThat(json.size()).isEqualTo(5)
}

fun valicateSessionCaptureApiModel(json: JsonObject) {
    failTest("")
}

fun validateScannerConnectionEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("SCANNER_CONNECTION")
    assertThat(json.get("relativeStartTime").asLong)
    with(json.get("scannerInfo").asJsonObject) {
        assertThat(get("scannerId").asString).isNotEmpty()
        assertThat(get("macAddress").asString).isNotEmpty()
        assertThat(get("hardwareVersion").asString).isNotEmpty()
        assertThat(get("generation").asString).isEqualTo("VERO_2")
    }
    assertThat(json.size()).isEqualTo(3)
}


fun validateVero2InfoSnapshotEventApiModel(json: JsonObject) {
    assertThat(json.get("type").asString).isEqualTo("VERO_2_INFO_SNAPSHOT")
    assertThat(json.get("relativeStartTime").asLong)

    with(json.get("version").asJsonObject) {
        assertThat(get("master").asString).isNotEmpty()
        assertThat(get("cypressApp").asString).isNotEmpty()
        assertThat(get("cypressApi").asString).isNotEmpty()
        assertThat(get("stmApp").asString).isNotEmpty()
        assertThat(get("stmApi").asString).isNotEmpty()
        assertThat(get("un20App").asString).isNotEmpty()
        assertThat(get("un20Api").asString).isNotEmpty()
    }

    with(json.get("battery").asJsonObject) {
        assertThat(get("charge").asInt)
        assertThat(get("voltage").asInt)
        assertThat(get("current").asInt)
        assertThat(get("temperature").asInt)
    }

    assertThat(json.size()).isEqualTo(4)
}

fun validateScannerFirmwareUpdateEventApiModel(json: JsonObject) {
    with(json) {
        assertThat(get("type").asString).isEqualTo("SCANNER_FIRMWARE_UPDATE")
        assertThat(get("relativeStartTime").asLong)
        assertThat(get("relativeEndTime").asLong)
        assertThat(get("chip").asString).isNotEmpty()
        assertThat(get("targetAppVersion").asString).isNotEmpty()
        assertThat(get("failureReason").asString).isNotEmpty()
    }
    assertThat(json.size()).isEqualTo(6)
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

private fun <T> Array<T>.valuesAsStrings(): List<String> = this.map { it.toString() }

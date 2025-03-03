package com.simprints.infra.eventsync.event

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.isValidGuid
import com.simprints.infra.eventsync.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType
import com.simprints.infra.eventsync.event.remote.models.ApiAuthenticationPayload
import com.simprints.infra.eventsync.event.remote.models.ApiRefusalPayload
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallbackType
import com.simprints.infra.eventsync.event.remote.models.callout.ApiCalloutType
import org.json.JSONObject

private val fingerIdentifiers = listOf(
    "LEFT_THUMB",
    "LEFT_INDEX_FINGER",
    "LEFT_3RD_FINGER",
    "LEFT_4TH_FINGER",
    "LEFT_5TH_FINGER",
    "RIGHT_THUMB",
    "RIGHT_INDEX_FINGER",
    "RIGHT_3RD_FINGER",
    "RIGHT_4TH_FINGER",
    "RIGHT_5TH_FINGER",
)

fun validateCommonParams(
    json: JSONObject,
    type: String,
    version: Int,
) {
    assertThat(json.getString("id")).isNotNull()
    assertThat(json.getString("type")).isEqualTo(type)
    assertThat(json.getInt("version")).isEqualTo(version)
    assertThat(json.length()).isEqualTo(5)
}

fun validateTimestamp(jsonObject: JSONObject) {
    assertThat(jsonObject.getInt("unixMs")).isNotNull()
    assertThat(jsonObject.getBoolean("isUnixMsTrustworthy")).isNotNull()
}

fun validateCallbackV1EventApiModel(json: JSONObject) {
    validateCommonParams(json, "Callback", 3)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        verifyCallbackPayloadContent(3)
    }
}

fun validateCallbackV2EventApiModel(json: JSONObject) {
    validateCommonParams(json, "Callback", 3)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        verifyCallbackPayloadContent(3)
    }
}

private fun JSONObject.verifyCallbackPayloadContent(version: Int) {
    with(getJSONObject("callback")) {
        when (ApiCallbackType.valueOf(getString("type"))) {
            ApiCallbackType.Enrolment -> verifyCallbackEnrolmentApiModel(this)
            ApiCallbackType.Identification -> verifyCallbackIdentificationApiModel(this, version)
            ApiCallbackType.Verification -> verifyCallbackVerificationApiModel(this, version)
            ApiCallbackType.Refusal -> verifyCallbackRefusalApiModel(this)
            ApiCallbackType.Confirmation -> verifyCallbackConfirmationApiModel(this)
            ApiCallbackType.Error -> verifyCallbackErrorApiModel(this)
        }
    }
}

fun verifyCallbackEnrolmentApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Enrolment")
    assertThat(json.getString("guid")).isNotNull()
    assertThat(json.length()).isEqualTo(2)
}

fun verifyCallbackIdentificationApiModel(
    json: JSONObject,
    version: Int,
) {
    assertThat(json.getString("type")).isEqualTo("Identification")
    assertThat(json.getString("sessionId")).isNotNull()
    json.getJSONArray("scores").let { jsonArray ->
        assertThat(jsonArray.length()).isEqualTo(1)
        val score = jsonArray.getJSONObject(0)

        assertThat(score.getString("guid")).isNotNull()
        assertThat(score.getString("confidence")).isNotNull()

        when (version) {
            1 -> assertThat(score.getString("tier")).isNotNull()
            else -> {}
        }

        when (version) {
            2 -> assertThat(score.has("confidenceMatch")).isFalse()
            3 -> assertThat(score.getString("confidenceMatch"))
            else -> {}
        }
    }
    assertThat(json.length()).isEqualTo(3)
}

fun verifyCallbackVerificationApiModel(
    json: JSONObject,
    version: Int,
) {
    assertThat(json.getString("type")).isEqualTo("Verification")
    json.getJSONObject("score").let { score ->
        assertThat(score.getString("guid")).isNotNull()
        assertThat(score.getString("confidence")).isNotNull()

        when (version) {
            1 -> assertThat(score.getString("tier")).isNotNull()
            else -> {}
        }

        when (version) {
            2 -> assertThat(score.has("confidenceMatch")).isFalse()
            3 -> assertThat(score.getString("confidenceMatch"))
            else -> {}
        }
    }
    assertThat(json.length()).isEqualTo(2)
}

fun verifyCallbackRefusalApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Refusal")
    assertThat(json.getString("reason")).isNotNull()
    assertThat(json.getString("extra")).isNotNull()
    assertThat(json.length()).isEqualTo(3)
}

fun verifyCallbackConfirmationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Confirmation")
    assertThat(json.getBoolean("received")).isNotNull()
    assertThat(json.length()).isEqualTo(2)
}

fun verifyCallbackErrorApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Error")
    assertThat(json.getString("reason")).isNotNull()
    assertThat(json.length()).isEqualTo(2)
}

fun validateCalloutEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Callout", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        with(getJSONObject("callout")) {
            when (ApiCalloutType.valueOf(getString("type"))) {
                ApiCalloutType.Confirmation -> verifyCalloutConfirmationApiModel(this)
                ApiCalloutType.Enrolment -> verifyCalloutEnrolmentApiModel(this)
                ApiCalloutType.Identification -> verifyCalloutIdentificationApiModel(this)
                ApiCalloutType.Verification -> verifyCalloutVerificationApiModel(this)
                ApiCalloutType.EnrolmentLastBiometrics -> verifyCalloutLastEnrolmentBiometricsApiModel(
                    this,
                )
            }
        }
        assertThat(length()).isEqualTo(2)
    }
}

fun verifyCalloutLastEnrolmentBiometricsApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("EnrolmentLastBiometrics")
    assertThat(json.getString("projectId")).isNotNull()
    assertThat(json.getString("userId")).isNotNull()
    assertThat(json.getString("moduleId")).isNotNull()
    assertThat(json.getString("metadata")).isNotNull()
    assertThat(json.getString("sessionId")).isNotNull()
    assertThat(json.length()).isEqualTo(6)
}

fun verifyCalloutVerificationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Verification")
    assertThat(json.getString("projectId")).isNotNull()
    assertThat(json.getString("userId")).isNotNull()
    assertThat(json.getString("moduleId")).isNotNull()
    assertThat(json.getString("verifyGuid")).isNotNull()
    assertThat(json.getString("metadata")).isNotNull()
    assertThat(json.length()).isEqualTo(6)
}

fun verifyCalloutIdentificationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Identification")
    assertThat(json.getString("projectId")).isNotNull()
    assertThat(json.getString("userId")).isNotNull()
    assertThat(json.getString("moduleId")).isNotNull()
    assertThat(json.getString("metadata")).isNotNull()
    assertThat(json.length()).isEqualTo(5)
}

fun verifyCalloutEnrolmentApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Enrolment")
    assertThat(json.getString("projectId")).isNotNull()
    assertThat(json.getString("userId")).isNotNull()
    assertThat(json.getString("moduleId")).isNotNull()
    assertThat(json.getString("metadata")).isNotNull()
    assertThat(json.length()).isEqualTo(5)
}

fun verifyCalloutConfirmationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Confirmation")
    assertThat(json.getString("selectedGuid")).isNotNull()
    assertThat(json.getString("sessionId")).isNotNull()
    assertThat(json.length()).isEqualTo(3)
}

fun validateAlertScreenEventApiModel(json: JSONObject) {
    validateCommonParams(json, "AlertScreen", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getString("alertType")).isIn(ApiAlertScreenEventType.values().valuesAsStrings())
        assertThat(length()).isEqualTo(2)
    }
}

fun validateAuthenticationEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Authentication", 2)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        with(getJSONObject("userInfo")) {
            assertThat(getString("projectId")).isNotEmpty()
            assertThat(getString("userId")).isNotEmpty()
            assertThat(length()).isEqualTo(2)
        }
        assertThat(getString("result")).isIn(
            ApiAuthenticationPayload.ApiResult.values().valuesAsStrings(),
        )
        assertThat(length()).isEqualTo(4)
    }
}

fun validateAuthorizationEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Authorization", 2)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        with(getJSONObject("userInfo")) {
            assertThat(getString("projectId")).isNotEmpty()
            assertThat(getString("userId")).isNotEmpty()
            assertThat(length()).isEqualTo(2)
        }
        assertThat(getString("result")).isAnyOf("AUTHORIZED", "NOT_AUTHORIZED")
        assertThat(length()).isEqualTo(3)
    }
}

fun validateCandidateReadEventApiModel(json: JSONObject) {
    validateCommonParams(json, "CandidateRead", 2)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("candidateId").isValidGuid()).isTrue()
        assertThat(getString("localResult")).isAnyOf("FOUND", "NOT_FOUND")
        if (has("remoteResult")) {
            assertThat(getString("remoteResult")).isAnyOf("FOUND", "NOT_FOUND")
            assertThat(length()).isEqualTo(5)
        } else {
            assertThat(length()).isEqualTo(4)
        }
    }
}

fun validateCompletionCheckEventApiModel(json: JSONObject) {
    validateCommonParams(json, "CompletionCheck", 2)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getBoolean("completed")).isNotNull()
        assertThat(length()).isEqualTo(2)
    }
}

fun validateConnectivitySnapshotEventApiModel(json: JSONObject) {
    validateCommonParams(json, "ConnectivitySnapshot", 3)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        val connections = getJSONArray("connections")
        for (i in 0 until connections.length()) {
            val connJson = connections.getJSONObject(i)
            assertThat(connJson.getString("type")).isNotEmpty()
            assertThat(connJson.getString("state")).isNotEmpty()
            assertThat(connJson.length()).isEqualTo(2)
        }
        assertThat(length()).isEqualTo(2)
    }
}

fun validateConsentEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Consent", 2)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("consentType")).isAnyOf("INDIVIDUAL", "PARENTAL")
        assertThat(getString("result")).isAnyOf("ACCEPTED", "DECLINED", "NO_RESPONSE")
        assertThat(length()).isEqualTo(4)
    }
}

fun validateEnrolmentEventV2ApiModel(json: JSONObject) {
    validateCommonParams(json, "Enrolment", 3)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getString("subjectId")).isNotNull()
        assertThat(getString("projectId")).isNotNull()
        assertThat(getString("moduleId")).isNotNull()
        assertThat(getString("attendantId")).isNotNull()
        assertThat(getString("personCreationEventId")).isNotNull()
        assertThat(length()).isEqualTo(6)
    }
}

fun validateEnrolmentEventV4ApiModel(json: JSONObject) {
    validateCommonParams(json, "Enrolment", 4)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getString("subjectId")).isNotNull()
        assertThat(getString("projectId")).isNotNull()
        assertThat(getString("moduleId")).isNotNull()
        assertThat(getString("attendantId")).isNotNull()
        assertThat(getJSONArray("biometricReferenceIds")).isNotNull()
        assertThat(length()).isEqualTo(6)
    }
}

fun validateIntentParsingEventApiModel(json: JSONObject) {
    validateCommonParams(json, "IntentParsing", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getString("integration")).isIn(listOf("STANDARD", "ODK", "COMMCARE"))
        assertThat(length()).isEqualTo(2)
    }
}

fun validateFingerprintCaptureEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FingerprintCapture", 4)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("id")).isNotNull()
        assertThat(getString("finger")).isIn(fingerIdentifiers)
        assertThat(getInt("qualityThreshold")).isNotNull()
        assertThat(getString("result")).isAnyOf(
            "GOOD_SCAN",
            "BAD_QUALITY",
            "NO_FINGER_DETECTED",
            "SKIPPED",
            "FAILURE_TO_ACQUIRE",
        )

        with(getJSONObject("fingerprint")) {
            assertThat(getString("finger")).isIn(fingerIdentifiers)
            assertThat(getInt("quality")).isNotNull()
            assertThat(getString("format")).isIn(listOf("ISO_19794_2", "NEC_1"))
            assertThat(length()).isEqualTo(3)
        }
        assertThat(length()).isEqualTo(7)
    }
}

fun validateGuidSelectionEventApiModel(json: JSONObject) {
    validateCommonParams(json, "GuidSelection", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getString("selectedId").isValidGuid()).isTrue()
        assertThat(length()).isEqualTo(2)
    }
}

fun validateMatchEntryApiModel(json: JSONObject) {
    assertThat(json.getString("candidateId").isValidGuid()).isTrue()
    assertThat(json.getInt("score")).isNotNull()
    assertThat(json.length()).isEqualTo(2)
}

fun validateOneToManyMatchEventApiModel(json: JSONObject) {
    validateCommonParams(json, "OneToManyMatch", 3)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("matcher")).isAnyOf("SIM_AFIS", "RANK_ONE")
        with(getJSONObject("pool")) {
            assertThat(getString("type")).isAnyOf("PROJECT", "MODULE", "USER")
            assertThat(getInt("count")).isNotNull()
            assertThat(length()).isEqualTo(2)
        }
        val matchEntries = getJSONArray("result")
        for (i in 0 until matchEntries.length()) {
            validateMatchEntryApiModel(matchEntries.getJSONObject(i))
        }
        assertThat(getString("probeBiometricReferenceId").isValidGuid()).isTrue()
        assertThat(length()).isEqualTo(6)
    }
}

fun validateOneToOneMatchEventApiModel(json: JSONObject) {
    validateCommonParams(json, "OneToOneMatch", 4)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("candidateId").isValidGuid()).isTrue()
        assertThat(getString("matcher")).isAnyOf("SIM_AFIS", "RANK_ONE")
        assertThat(getString("fingerComparisonStrategy")).isAnyOf(
            "null",
            "SAME_FINGER",
            "CROSS_FINGER_USING_MEAN_OF_MAX",
        )
        with(getJSONObject("result")) {
            validateMatchEntryApiModel(this)
        }
        assertThat(getString("probeBiometricReferenceId").isValidGuid()).isTrue()
        assertThat(length()).isEqualTo(7)
    }
}

fun validatePersonCreationEvent(json: JSONObject) {
    validateCommonParams(json, "PersonCreation", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getString("fingerprintReferenceId")).isNotNull()
        val fingerprintCaptureIds = getJSONArray("fingerprintCaptureIds")
        for (i in 0 until fingerprintCaptureIds.length()) {
            assertThat(fingerprintCaptureIds.getString(i).isValidGuid()).isTrue()
        }

        assertThat(getString("faceReferenceId")).isNotNull()
        val faceCaptureIds = getJSONArray("faceCaptureIds")
        for (i in 0 until faceCaptureIds.length()) {
            assertThat(faceCaptureIds.getString(i).isValidGuid()).isTrue()
        }
        assertThat(length()).isEqualTo(5)
    }
}

fun validateRefusalEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Refusal", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("reason")).isIn(ApiRefusalPayload.ApiAnswer.values().valuesAsStrings())
        assertThat(getString("otherText")).isNotNull()
        assertThat(length()).isEqualTo(4)
    }
}

fun validateScannerConnectionEventApiModel(json: JSONObject) {
    validateCommonParams(json, "ScannerConnection", 2)

    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        with(getJSONObject("scannerInfo")) {
            assertThat(getString("scannerId")).isNotEmpty()
            assertThat(getString("macAddress")).isNotEmpty()
            assertThat(getString("hardwareVersion")).isNotEmpty()
            assertThat(getString("generation")).isAnyOf("VERO_2", "VERO_1")
            assertThat(length()).isEqualTo(4)
        }
        assertThat(length()).isEqualTo(2)
    }
}

fun validateVero2InfoSnapshotEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Vero2InfoSnapshot", 3)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))

        with(getJSONObject("scannerVersion")) {
            assertThat(getString("hardwareRevision")).isNotEmpty()
            assertThat(getString("cypressApp")).isNotEmpty()
            assertThat(getString("stmApp")).isNotEmpty()
            assertThat(getString("un20App")).isNotEmpty()
            assertThat(getString("master")).isNotEmpty()
            assertThat(length()).isEqualTo(5)
        }

        with(getJSONObject("battery")) {
            assertThat(getInt("charge")).isNotNull()
            assertThat(getInt("voltage")).isNotNull()
            assertThat(getInt("current")).isNotNull()
            assertThat(getInt("temperature")).isNotNull()
            assertThat(length()).isEqualTo(4)
        }

        assertThat(length()).isEqualTo(3)
    }
}

fun validateScannerFirmwareUpdateEventApiModel(json: JSONObject) {
    validateCommonParams(json, "ScannerFirmwareUpdate", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("chip")).isNotEmpty()
        assertThat(getString("targetAppVersion")).isNotEmpty()
        assertThat(getString("failureReason")).isNotEmpty()
        assertThat(length()).isEqualTo(5)
    }
}

fun validateSuspiciousIntentEventApiModel(json: JSONObject) {
    validateCommonParams(json, "SuspiciousIntent", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getJSONObject("unexpectedExtras").toString()).isNotNull()
        assertThat(length()).isEqualTo(2)
    }
}

fun validateInvalidEventApiModel(json: JSONObject) {
    validateCommonParams(json, "InvalidIntent", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getJSONObject("extras").toString()).isNotNull()
        assertThat(getString("action")).isNotNull()
        assertThat(length()).isEqualTo(3)
    }
}

fun validateFaceOnboardingCompleteEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceOnboardingComplete", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(length()).isEqualTo(2)
    }
}

fun validateFaceFallbackCaptureEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceFallbackCapture", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(length()).isEqualTo(2)
    }
}

fun validateFaceCaptureEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceCapture", 4)
    with(json.getJSONObject("payload")) {
        assertThat(getString("id")).isNotNull()
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getInt("attemptNb")).isNotNull()
        assertThat(getDouble("qualityThreshold")).isNotNull()
        assertThat(getString("result")).isIn(
            listOf(
                "VALID",
                "INVALID",
                "OFF_YAW",
                "OFF_ROLL",
                "TOO_CLOSE",
                "TOO_FAR",
            ),
        )
        assertThat(getBoolean("isFallback")).isNotNull()
        val face = getJSONObject("face")
        with(face) {
            assertThat(getDouble("yaw")).isNotNull()
            assertThat(getDouble("roll")).isNotNull()
            assertThat(getDouble("quality")).isNotNull()
            assertThat(getString("format")).isIn(listOf("RANK_ONE_1_23"))
            assertThat(length()).isEqualTo(4)
        }

        assertThat(length()).isEqualTo(8)
    }
}

fun validateFaceCaptureConfirmationEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceCaptureConfirmation", 2)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("result")).isIn(listOf("CONTINUE", "RECAPTURE"))
        assertThat(length()).isEqualTo(3)
    }
}

fun validateFaceCaptureBiometricsEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceCaptureBiometrics", 1)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(length()).isEqualTo(3)
    }
}

fun validateFingerprintCaptureBiometricsEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FingerprintCaptureBiometrics", 1)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(length()).isEqualTo(3)
    }
}

fun validateDownSyncRequestEventApiModel(json: JSONObject) {
    validateCommonParams(json, "EventDownSyncRequest", 0)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("requestId")).isNotNull()
        assertThat(getString("queryParameters")).isNotNull()
        assertThat(getInt("responseStatus")).isNotNull()
        assertThat(getString("errorType")).isNotNull()
        assertThat(getInt("msToFirstResponseByte")).isNotNull()
        assertThat(getInt("eventsRead")).isNotNull()
    }
}

fun validateUpSyncRequestEventApiModel(json: JSONObject) {
    validateCommonParams(json, "EventUpSyncRequest", 0)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("requestId")).isNotNull()
        assertThat(getString("content")).isNotNull()
        assertThat(getInt("responseStatus")).isNotNull()
        assertThat(getString("errorType")).isNotNull()
    }
}

fun validateAgeGroupSelectionEventApiModel(json: JSONObject) {
    validateCommonParams(json, "AgeGroupSelection", 1)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        validateTimestamp(getJSONObject("endTime"))
        assertThat(getString("subjectAgeGroup")).isNotNull()
    }
}

fun validateBiometricReferenceCreationEventApiModel(json: JSONObject) {
    validateCommonParams(json, "BiometricReferenceCreation", 1)
    with(json.getJSONObject("payload")) {
        validateTimestamp(getJSONObject("startTime"))
        assertThat(getString("id")).isNotNull()
        assertThat(getString("modality")).isNotNull()
        assertThat(getString("captureIds")).isNotNull()
    }
}

private fun <T> Array<T>.valuesAsStrings(): List<String> = this.map { it.toString() }

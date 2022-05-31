package com.simprints.eventsystem

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.isGuid
import com.simprints.eventsystem.event.remote.models.ApiAlertScreenPayload.ApiAlertScreenEventType
import com.simprints.eventsystem.event.remote.models.ApiArtificialTerminationPayload.ApiReason
import com.simprints.eventsystem.event.remote.models.ApiAuthenticationPayload
import com.simprints.eventsystem.event.remote.models.ApiRefusalPayload
import com.simprints.eventsystem.event.remote.models.callback.ApiCallbackType
import com.simprints.eventsystem.event.remote.models.callout.ApiCalloutType
import org.json.JSONArray
import org.json.JSONObject

private val fingerIdentifiers = listOf("LEFT_THUMB", "LEFT_INDEX_FINGER", "LEFT_3RD_FINGER", "LEFT_4TH_FINGER", "LEFT_5TH_FINGER", "RIGHT_THUMB", "RIGHT_INDEX_FINGER", "RIGHT_3RD_FINGER", "RIGHT_4TH_FINGER", "RIGHT_5TH_FINGER")

fun validateCommonParams(json: JSONObject, type: String) {
    assertThat(json.getString("id")).isNotNull()
    with(json.getJSONObject("labels")) {
        assertThat(this.getJSONArray("sessionId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("deviceId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("projectId").length()).isEqualTo(1)
        assertThat(this.length()).isEqualTo(3)
    }
    with(json.getJSONObject("payload")) {
        assertThat(getString("type")).isEqualTo(type)
    }
    assertThat(json.length()).isEqualTo(3)
}

fun validateCallbackEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Callback")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        with(getJSONObject("callback")) {
            when (ApiCallbackType.valueOf(getString("type"))) {
                ApiCallbackType.Enrolment -> verifyCallbackEnrolmentApiModel(this)
                ApiCallbackType.Identification -> verifyCallbackIdentificationApiModel(this)
                ApiCallbackType.Verification -> verifyCallbackVerificationApiModel(this)
                ApiCallbackType.Refusal -> verifyCallbackRefusalApiModel(this)
                ApiCallbackType.Confirmation -> verifyCallbackConfirmationApiModel(this)
                ApiCallbackType.Error -> verifyCallbackErrorApiModel(this)
            }
        }
        assertThat(length()).isEqualTo(4)
    }
}

fun verifyCallbackEnrolmentApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Enrolment")
    assertThat(json.getString("guid"))
    assertThat(json.length()).isEqualTo(2)
}

fun verifyCallbackIdentificationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Identification")
    assertThat(json.getString("sessionId"))
    verifyCallbackIdentificationScoresApiModel(json.getJSONArray("scores"))
    assertThat(json.length()).isEqualTo(3)
}

fun verifyCallbackIdentificationScoresApiModel(jsonArray: JSONArray) {
    val score = jsonArray.getJSONObject(0)
    assertThat(score.getString("guid"))
    assertThat(score.getString("tier"))
    assertThat(score.getString("confidence"))
    assertThat(jsonArray.length()).isEqualTo(1)
}

fun verifyCallbackVerificationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Verification")
    with(json.getJSONObject("score")) {
        assertThat(getString("guid"))
        assertThat(getString("confidence"))
        assertThat(getString("tier"))
        assertThat(length()).isEqualTo(3)
    }
    assertThat(json.length()).isEqualTo(2)
}

fun verifyCallbackRefusalApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Refusal")
    assertThat(json.getString("reason")).isNotNull()
    assertThat(json.getString("extra"))
    assertThat(json.length()).isEqualTo(3)
}

fun verifyCallbackConfirmationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Confirmation")
    assertThat(json.getBoolean("received"))
    assertThat(json.length()).isEqualTo(2)
}

fun verifyCallbackErrorApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Error")
    assertThat(json.getString("reason"))
    assertThat(json.length()).isEqualTo(2)
}

fun validateCalloutEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Callout")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        with(getJSONObject("callout")) {
            when (ApiCalloutType.valueOf(getString("type"))) {
                ApiCalloutType.Confirmation -> verifyCalloutConfirmationApiModel(this)
                ApiCalloutType.Enrolment -> verifyCalloutEnrolmentApiModel(this)
                ApiCalloutType.Identification -> verifyCalloutIdentificationApiModel(this)
                ApiCalloutType.Verification -> verifyCalloutVerificationApiModel(this)
                ApiCalloutType.EnrolmentLastBiometrics -> verifyCalloutLastEnrolmentBiometricsApiModel(this)
            }
        }
        assertThat(length()).isEqualTo(4)
    }
}

fun verifyCalloutLastEnrolmentBiometricsApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("EnrolmentLastBiometrics")
    assertThat(json.getString("projectId"))
    assertThat(json.getString("userId"))
    assertThat(json.getString("moduleId"))
    assertThat(json.getString("metadata"))
    assertThat(json.getString("sessionId"))
    assertThat(json.length()).isEqualTo(6)
}

fun verifyCalloutVerificationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Verification")
    assertThat(json.getString("projectId"))
    assertThat(json.getString("userId"))
    assertThat(json.getString("moduleId"))
    assertThat(json.getString("verifyGuid"))
    assertThat(json.getString("metadata"))
    assertThat(json.length()).isEqualTo(6)
}

fun verifyCalloutIdentificationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Identification")
    assertThat(json.getString("projectId"))
    assertThat(json.getString("userId"))
    assertThat(json.getString("moduleId"))
    assertThat(json.getString("metadata"))
    assertThat(json.length()).isEqualTo(5)
}

fun verifyCalloutEnrolmentApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Enrolment")
    assertThat(json.getString("projectId"))
    assertThat(json.getString("userId"))
    assertThat(json.getString("moduleId"))
    assertThat(json.getString("metadata"))
    assertThat(json.length()).isEqualTo(5)
}

fun verifyCalloutConfirmationApiModel(json: JSONObject) {
    assertThat(json.getString("type")).isEqualTo("Confirmation")
    assertThat(json.getString("selectedGuid"))
    assertThat(json.getString("sessionId"))
    assertThat(json.length()).isEqualTo(3)
}

fun validateAlertScreenEventApiModel(json: JSONObject) {
    validateCommonParams(json, "AlertScreen")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        assertThat(getString("alertType")).isIn(ApiAlertScreenEventType.values().valuesAsStrings())
        assertThat(length()).isEqualTo(4)
    }
}

fun validateArtificialTerminationEventApiModel(json: JSONObject) {
    validateCommonParams(json, "ArtificialTermination")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        assertThat(getString("reason")).isIn(ApiReason.values().valuesAsStrings())
        assertThat(length()).isEqualTo(4)
    }
}

fun validateAuthenticationEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Authentication")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        assertThat(getLong("endTime"))
        with(getJSONObject("userInfo")) {
            assertThat(getString("projectId")).isNotEmpty()
            assertThat(getString("userId")).isNotEmpty()
            assertThat(length()).isEqualTo(2)
        }
        assertThat(getString("result")).isIn(ApiAuthenticationPayload.ApiResult.values().valuesAsStrings())
        assertThat(length()).isEqualTo(6)
    }
}

fun validateAuthorizationEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Authorization")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        with(getJSONObject("userInfo")) {
            assertThat(getString("projectId")).isNotEmpty()
            assertThat(getString("userId")).isNotEmpty()
            assertThat(length()).isEqualTo(2)
        }
        assertThat(getString("result")).isAnyOf("AUTHORIZED", "NOT_AUTHORIZED")
        assertThat(length()).isEqualTo(5)
    }
}

fun validateCandidateReadEventApiModel(json: JSONObject) {
    validateCommonParams(json, "CandidateRead")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        assertThat(getLong("endTime"))
        assertThat(getString("candidateId").isGuid()).isTrue()
        assertThat(getString("localResult")).isAnyOf("FOUND", "NOT_FOUND")
        if (has("remoteResult")) {
            assertThat(getString("remoteResult")).isAnyOf("FOUND", "NOT_FOUND")
            assertThat(length()).isEqualTo(7)
        } else {
            assertThat(length()).isEqualTo(6)
        }
    }
}

fun validateCompletionCheckEventApiModel(json: JSONObject) {
    validateCommonParams(json, "CompletionCheck")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        assertThat(getBoolean("completed"))
        assertThat(length()).isEqualTo(4)
    }
}

fun validateConnectivitySnapshotEventApiModel(json: JSONObject) {
    validateCommonParams(json, "ConnectivitySnapshot")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(2)
        assertThat(getInt("startTime"))
        val connections = getJSONArray("connections")
        for (i in 0 until connections.length()) {
            val connJson = connections.getJSONObject(i)
            assertThat(connJson.getString("type")).isNotEmpty()
            assertThat(connJson.getString("state")).isNotEmpty()
            assertThat(connJson.length()).isEqualTo(2)
        }
        assertThat(length()).isEqualTo(4)
    }
}


fun validateConsentEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Consent")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getInt("startTime"))
        assertThat(getLong("endTime"))
        assertThat(getString("consentType")).isAnyOf("INDIVIDUAL", "PARENTAL")
        assertThat(getString("result")).isAnyOf("ACCEPTED", "DECLINED", "NO_RESPONSE")
        assertThat(length()).isEqualTo(6)
    }
}

fun validateEnrolmentEventV1ApiModel(json: JSONObject) {
    validateCommonParams(json, "Enrolment")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime"))
        assertThat(getString("personId").isGuid()).isTrue()
        assertThat(length()).isEqualTo(4)
    }
}

fun validateEnrolmentEventV2ApiModel(json: JSONObject) {
    validateCommonParams(json, "Enrolment")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(2)
        assertThat(getLong("startTime"))
        assertThat(getString("subjectId"))
        assertThat(getString("projectId"))
        assertThat(getString("moduleId"))
        assertThat(getString("attendantId"))
        assertThat(getString("personCreationEventId"))
        assertThat(length()).isEqualTo(8)
    }
}

fun validateEnrolmentRecordCreationEventApiModel(json: JSONObject) {
    assertThat(json.getString("id")).isNotNull()
    with(json.getJSONObject("labels")) {
        assertThat(this.getJSONArray("projectId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("moduleId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("attendantId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("mode").length()).isEqualTo(2)
        assertThat(this.length()).isEqualTo(4)
    }
    with(json.getJSONObject("payload")) {
        assertThat(getString("type")).isEqualTo("EnrolmentRecordCreation")
    }
    assertThat(json.length()).isEqualTo(3)

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(3)
        assertThat(getString("projectId")).isNotEmpty()
        assertThat(getString("moduleId")).isNotEmpty()
        assertThat(getString("attendantId")).isNotEmpty()
        assertThat(getLong("startTime"))
        val references = getJSONArray("biometricReferences")
        validateBiometricReferences(references)

        assertThat(length()).isEqualTo(8)
    }
}


fun validateEnrolmentRecordDeletionEventApiModel(json: JSONObject) {
    assertThat(json.getString("id")).isNotNull()
    with(json.getJSONObject("labels")) {
        assertThat(this.getJSONArray("projectId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("moduleId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("attendantId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("mode").length()).isEqualTo(2)
        assertThat(this.length()).isEqualTo(4)
    }
    with(json.getJSONObject("payload")) {
        assertThat(getString("type")).isEqualTo("EnrolmentRecordDeletion")
    }
    assertThat(json.length()).isEqualTo(3)

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(0)
        assertThat(getString("projectId")).isNotEmpty()
        assertThat(getString("moduleId")).isNotEmpty()
        assertThat(getString("attendantId")).isNotEmpty()
        assertThat(length()).isEqualTo(6)
    }
}


fun validateEnrolmentRecordMoveEventApiModel(json: JSONObject) {
    assertThat(json.getString("id")).isNotNull()
    with(json.getJSONObject("labels")) {
        assertThat(this.getJSONArray("projectId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("moduleId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("attendantId").length()).isEqualTo(1)
        assertThat(this.getJSONArray("mode").length()).isEqualTo(2)
        assertThat(this.length()).isEqualTo(4)
    }
    with(json.getJSONObject("payload")) {
        assertThat(getString("type")).isEqualTo("EnrolmentRecordMove")
    }
    assertThat(json.length()).isEqualTo(3)

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        val creation = getJSONObject("enrolmentRecordCreation")
        with(creation) {
            assertThat(getString("projectId")).isNotEmpty()
            assertThat(getString("moduleId")).isNotEmpty()
            assertThat(getString("attendantId")).isNotEmpty()
            validateBiometricReferences(getJSONArray("biometricReferences"))
        }

        val deletion = getJSONObject("enrolmentRecordDeletion")
        with(deletion) {
            assertThat(getString("projectId")).isNotEmpty()
            assertThat(getString("moduleId")).isNotEmpty()
            assertThat(getString("attendantId")).isNotEmpty()
        }

        assertThat(length()).isEqualTo(4)
    }
}

fun validateBiometricReferences(references: JSONArray) {
    with(references) {
        assertThat(length()).isEqualTo(2)
        val fingerprint = references[0] as JSONObject
        val face = references[1] as JSONObject
        with(fingerprint) {
            assertThat(getString("type")).isEqualTo("FingerprintReference")
            assertThat(getJSONObject("metadata")).isNotNull()
            val templates = getJSONArray("templates")
            for (i in 0 until templates.length()) {
                val template = templates.getJSONObject(i)
                assertThat(template.getString("template")).isNotEmpty()
                assertThat(template.getLong("quality")).isNotNull()
                assertThat(template.getString("finger")).isIn(fingerIdentifiers)
            }
        }

        with(face) {
            assertThat(getString("type")).isEqualTo("FaceReference")
            val templates = getJSONArray("templates")
            for (i in 0 until templates.length()) {
                val template = templates.getJSONObject(i)
                assertThat(template.getString("template")).isNotEmpty()
            }
        }
    }
}


fun validateIntentParsingEventApiModel(json: JSONObject) {
    validateCommonParams(json, "IntentParsing")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime"))
        assertThat(getString("integration")).isIn(listOf("STANDARD", "ODK", "COMMCARE"))
        assertThat(length()).isEqualTo(4)
    }
}

fun validateFingerprintCaptureEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FingerprintCapture")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(2)
        assertThat(getLong("startTime"))
        assertThat(getLong("endTime"))
        assertThat(getString("id"))
        assertThat(getString("finger")).isIn(fingerIdentifiers)
        assertThat(getInt("qualityThreshold"))
        assertThat(getString("result")).isAnyOf("GOOD_SCAN", "BAD_QUALITY", "NO_FINGER_DETECTED", "SKIPPED", "FAILURE_TO_ACQUIRE")

        with(getJSONObject("fingerprint")) {
            assertThat(getString("finger")).isIn(fingerIdentifiers)
            assertThat(getInt("quality"))
            assertThat(getString("template")).isNotEmpty()
            assertThat(getString("format")).isIn(listOf("ISO_19794_2", "NEC"))
            assertThat(length()).isEqualTo(4)
        }
        assertThat(length()).isEqualTo(9)
    }
}

fun validateGuidSelectionEventApiModel(json: JSONObject) {
    validateCommonParams(json, "GuidSelection")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime"))
        assertThat(getString("selectedId").isGuid()).isTrue()
        assertThat(length()).isEqualTo(4)
    }
}

fun validateMatchEntryApiModel(json: JSONObject) {
    assertThat(json.getString("candidateId").isGuid()).isTrue()
    assertThat(json.getInt("score"))
    assertThat(json.length()).isEqualTo(2)
}

fun validateOneToManyMatchEventApiModel(json: JSONObject) {
    validateCommonParams(json, "OneToManyMatch")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime"))
        assertThat(getLong("endTime"))
        assertThat(getString("matcher")).isAnyOf("SIM_AFIS", "RANK_ONE")
        with(getJSONObject("pool")) {
            assertThat(getString("type")).isAnyOf("PROJECT", "MODULE", "USER")
            assertThat(getInt("count"))
            assertThat(length()).isEqualTo(2)
        }
        val matchEntries = getJSONArray("result")
        for (i in 0 until matchEntries.length()) {
            validateMatchEntryApiModel(matchEntries.getJSONObject(i))
        }
        assertThat(length()).isEqualTo(7)
    }
}

fun validateOneToOneMatchEventApiModel(json: JSONObject) {
    validateCommonParams(json, "OneToOneMatch")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(2)
        assertThat(getLong("startTime"))
        assertThat(getLong("endTime"))
        assertThat(getString("candidateId").isGuid()).isTrue()
        assertThat(getString("matcher")).isAnyOf("SIM_AFIS", "RANK_ONE")
        assertThat(getString("fingerComparisonStrategy")).isAnyOf(
            "null", "SAME_FINGER","CROSS_FINGER_USING_MEAN_OF_MAX")
        with(getJSONObject("result")) {
            validateMatchEntryApiModel(this)
        }
        assertThat(length()).isEqualTo(8)
    }
}

fun validatePersonCreationEvent(json: JSONObject) {
    validateCommonParams(json, "PersonCreation")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getString("fingerprintReferenceId")).isNotNull()
        val fingerprintCaptureIds = getJSONArray("fingerprintCaptureIds")
        for (i in 0 until fingerprintCaptureIds.length()) {
            assertThat(fingerprintCaptureIds.getString(i).isGuid()).isTrue()
        }

        assertThat(getString("faceReferenceId")).isNotNull()
        val faceCaptureIds = getJSONArray("faceCaptureIds")
        for (i in 0 until faceCaptureIds.length()) {
            assertThat(faceCaptureIds.getString(i).isGuid()).isTrue()
        }
        assertThat(length()).isEqualTo(7)
    }
}

fun validateRefusalEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Refusal")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getLong("endTime")).isNotNull()
        assertThat(getString("reason")).isIn(ApiRefusalPayload.ApiAnswer.values().valuesAsStrings())
        assertThat(getString("otherText")).isNotNull()
        assertThat(length()).isEqualTo(6)
    }
}

fun validateSessionCaptureApiModel(json: JSONObject) {
    validateCommonParams(json, "SessionCapture")
    with(json.getJSONObject("labels")) {
        assertThat(getJSONArray("sessionId").get(0).toString()).isEqualTo(json.getString("id"))
    }

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getString("id")).isNotNull()
        assertThat(getString("projectId")).isNotNull()
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getLong("endTime")).isNotNull()
        assertThat(getLong("uploadTime")).isNotNull()
        assertThat(getJSONArray("modalities").length()).isEqualTo(2)
        assertThat(getString("appVersionName")).isNotNull()
        assertThat(getString("libVersionName")).isNotNull()
        assertThat(getString("language")).isNotNull()
        validateDeviceApiModel(getJSONObject("device"))
        validateDatabaseInfoApiModel(getJSONObject("databaseInfo"))
        validateLocationApiModel(getJSONObject("location"))

        assertThat(length()).isEqualTo(14)
    }
}

fun validateScannerConnectionEventApiModel(json: JSONObject) {
    validateCommonParams(json, "ScannerConnection")

    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime"))
        with(getJSONObject("scannerInfo")) {
            assertThat(getString("scannerId")).isNotEmpty()
            assertThat(getString("macAddress")).isNotEmpty()
            assertThat(getString("hardwareVersion")).isNotEmpty()
            assertThat(getString("generation")).isAnyOf("VERO_2", "VERO_1")
            assertThat(length()).isEqualTo(4)
        }
        assertThat(length()).isEqualTo(4)
    }
}


fun validateVero2InfoSnapshotEventApiModel(json: JSONObject) {
    validateCommonParams(json, "Vero2InfoSnapshot")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime"))

        with(getJSONObject("scannerVersion")) {
            assertThat(getString("master")).isNotEmpty()
            assertThat(getString("cypressApp")).isNotEmpty()
            assertThat(getString("cypressApi")).isNotEmpty()
            assertThat(getString("stmApp")).isNotEmpty()
            assertThat(getString("stmApi")).isNotEmpty()
            assertThat(getString("un20App")).isNotEmpty()
            assertThat(getString("un20Api")).isNotEmpty()
            assertThat(length()).isEqualTo(7)
        }

        with(getJSONObject("battery")) {
            assertThat(getInt("charge"))
            assertThat(getInt("voltage"))
            assertThat(getInt("current"))
            assertThat(getInt("temperature"))
            assertThat(length()).isEqualTo(4)
        }

        assertThat(length()).isEqualTo(5)
    }
}

fun validateScannerFirmwareUpdateEventApiModel(json: JSONObject) {
    validateCommonParams(json, "ScannerFirmwareUpdate")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime"))
        assertThat(getLong("endTime"))
        assertThat(getString("chip")).isNotEmpty()
        assertThat(getString("targetAppVersion")).isNotEmpty()
        assertThat(getString("failureReason")).isNotEmpty()
        assertThat(length()).isEqualTo(7)
    }
}

fun validateDatabaseInfoApiModel(json: JSONObject) {
    assertThat(json.getInt("recordCount"))
    assertThat(json.getInt("sessionCount"))
    assertThat(json.length()).isEqualTo(2)
}

fun validateDeviceApiModel(json: JSONObject) {
    assertThat(json.getString("androidSdkVersion")).isNotEmpty()
    assertThat(json.getString("deviceModel")).isNotEmpty()
    assertThat(json.getString("deviceId")).isNotEmpty()
    assertThat(json.length()).isEqualTo(3)
}

fun validateLocationApiModel(json: JSONObject) {
    assertThat(json.getDouble("latitude"))
    assertThat(json.getDouble("longitude"))
    assertThat(json.length()).isEqualTo(2)
}

fun validateSuspiciousIntentEventApiModel(json: JSONObject) {
    validateCommonParams(json, "SuspiciousIntent")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getJSONObject("unexpectedExtras").toString()).isNotNull()
        assertThat(length()).isEqualTo(4)
    }
}

fun validateInvalidEventApiModel(json: JSONObject) {
    validateCommonParams(json, "InvalidIntent")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getJSONObject("extras").toString()).isNotNull()
        assertThat(getString("action")).isNotNull()
        assertThat(length()).isEqualTo(5)
    }
}

fun validateFaceOnboardingCompleteEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceOnboardingComplete")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getLong("endTime")).isNotNull()
        assertThat(length()).isEqualTo(4)
    }
}


fun validateFaceFallbackCaptureEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceFallbackCapture")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getLong("endTime")).isNotNull()
        assertThat(length()).isEqualTo(4)
    }
}

fun validateFaceCaptureEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceCapture")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(2)
        assertThat(getString("id")).isNotNull()
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getLong("endTime")).isNotNull()
        assertThat(getInt("attemptNb")).isNotNull()
        assertThat(getDouble("qualityThreshold")).isNotNull()
        assertThat(getString("result")).isIn(listOf("VALID", "INVALID", "OFF_YAW", "OFF_ROLL", "TOO_CLOSE", "TOO_FAR"))
        assertThat(getBoolean("isFallback")).isNotNull()
        val face = getJSONObject("face")
        with(face) {
            assertThat(getDouble("yaw")).isNotNull()
            assertThat(getDouble("roll")).isNotNull()
            assertThat(getDouble("quality")).isNotNull()
            assertThat(getString("template")).isNotNull()
            assertThat(getString("format")).isIn(listOf("RANK_ONE_1_23"))
            assertThat(length()).isEqualTo(5)
        }

        assertThat(length()).isEqualTo(10)
    }
}

fun validateFaceCaptureConfirmationEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceCaptureConfirmation")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(1)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getLong("endTime")).isNotNull()
        assertThat(getString("result")).isIn(listOf("CONTINUE", "RECAPTURE"))
        assertThat(length()).isEqualTo(5)
    }
}

fun validateFaceCaptureBiometricsEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FaceCaptureBiometrics")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(0)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getString("result")).isIn(listOf("VALID", "INVALID", "OFF_YAW", "OFF_ROLL", "TOO_CLOSE", "TOO_FAR"))
        assertThat(length()).isEqualTo(7)
    }
}

fun validateFingerprintCaptureBiometricsEventApiModel(json: JSONObject) {
    validateCommonParams(json, "FingerprintCaptureBiometrics")
    with(json.getJSONObject("payload")) {
        assertThat(getInt("version")).isEqualTo(0)
        assertThat(getLong("startTime")).isNotNull()
        assertThat(getString("result")).isAnyOf("GOOD_SCAN", "BAD_QUALITY", "NO_FINGER_DETECTED", "SKIPPED", "FAILURE_TO_ACQUIRE")
        assertThat(length()).isEqualTo(7)
    }
}
private fun <T> Array<T>.valuesAsStrings(): List<String> = this.map { it.toString() }

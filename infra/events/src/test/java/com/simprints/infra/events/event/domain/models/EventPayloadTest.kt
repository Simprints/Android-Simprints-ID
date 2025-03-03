package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtils.Connection
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult.NOT_FOUND
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo
import com.simprints.infra.events.event.domain.models.callback.CallbackComparisonScore
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEvent
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent.QueryParameters
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.CONTINUE
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceOnboardingCompleteEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo as AuthenticationUserInfo
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo as AuthorizationUserInfo

class EventPayloadTest {
    @Test
    fun `safe string does not include sensitive data`() {
        val sensitiveInfoList = listOf(
            DEFAULT_USER_ID.value,
            DEFAULT_PROJECT_ID,
            "template",
        )

        allEventsList().forEach {
            val safeString = it.payload.toSafeString()
            sensitiveInfoList.forEach { assertThat(safeString).doesNotContain(it) }
        }
    }

    private fun allEventsList(): List<Event> = listOf(
        ConfirmationCallbackEvent(CREATED_AT, true),
        EnrolmentCallbackEvent(CREATED_AT, GUID1),
        ErrorCallbackEvent(CREATED_AT, Reason.DIFFERENT_PROJECT_ID_SIGNED_IN),
        IdentificationCallbackEvent(
            createdAt = CREATED_AT,
            sessionId = GUID1,
            scores = listOf(CallbackComparisonScore(GUID1, 1, AppMatchConfidence.NONE)),
        ),
        RefusalCallbackEvent(CREATED_AT, "some_reason", "some_extra"),
        VerificationCallbackEvent(
            createdAt = CREATED_AT,
            score = CallbackComparisonScore(GUID1, 1, AppMatchConfidence.NONE),
        ),
        ConfirmationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, GUID1, GUID2),
        EnrolmentCalloutEvent(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_MODULE_ID,
            metadata = DEFAULT_METADATA,
        ),
        EnrolmentLastBiometricsCalloutEvent(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_MODULE_ID,
            metadata = DEFAULT_METADATA,
            sessionId = GUID1,
        ),
        IdentificationCalloutEvent(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_MODULE_ID,
            metadata = DEFAULT_METADATA,
        ),
        VerificationCalloutEvent(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_MODULE_ID,
            verifyGuid = GUID1,
            metadata = DEFAULT_METADATA,
        ),
        EventDownSyncRequestEvent(
            createdAt = CREATED_AT,
            endedAt = ENDED_AT,
            query = QueryParameters(
                moduleId = DEFAULT_MODULE_ID.value,
                attendantId = DEFAULT_USER_ID.value,
            ),
            requestId = "requestId",
        ),
        FaceCaptureBiometricsEvent(
            startTime = CREATED_AT,
            face = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
                yaw = 0.0f,
                roll = 1.0f,
                template = "template",
                quality = 1.0f,
                format = FACE_TEMPLATE_FORMAT,
            ),
        ),
        FaceCaptureConfirmationEvent(CREATED_AT, ENDED_AT, CONTINUE),
        FaceCaptureEvent(
            startTime = CREATED_AT,
            endTime = ENDED_AT,
            attemptNb = 0,
            qualityThreshold = 1F,
            result = FaceCaptureEvent.FaceCapturePayload.Result.VALID,
            isAutoCapture = false,
            isFallback = true,
            face = FaceCaptureEvent.FaceCapturePayload.Face(0F, 1F, 2F, FACE_TEMPLATE_FORMAT),
        ),
        FaceFallbackCaptureEvent(CREATED_AT, ENDED_AT),
        FaceOnboardingCompleteEvent(CREATED_AT, ENDED_AT),
        FingerprintCaptureBiometricsEvent(
            createdAt = CREATED_AT,
            fingerprint = FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
                finger = IFingerIdentifier.LEFT_3RD_FINGER,
                template = "template",
                quality = 1,
                format = "ISO_19794_2",
            ),
            id = "someId",
        ),
        FingerprintCaptureEvent(
            createdAt = CREATED_AT,
            endTime = ENDED_AT,
            finger = IFingerIdentifier.LEFT_THUMB,
            qualityThreshold = 10,
            result = FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY,
            fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
                finger = IFingerIdentifier.LEFT_THUMB,
                quality = 8,
                format = "ISO_19794_2",
            ),
        ),
        AlertScreenEvent(CREATED_AT, BLUETOOTH_NOT_ENABLED),
        AuthenticationEvent(
            createdAt = CREATED_AT,
            endTime = ENDED_AT,
            userInfo = AuthenticationUserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID),
            result = AuthenticationPayload.Result.INTEGRITY_SERVICE_ERROR,
        ),
        AuthorizationEvent(
            createdAt = CREATED_AT,
            result = AUTHORIZED,
            userInfo = AuthorizationUserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID),
        ),
        CandidateReadEvent(CREATED_AT, ENDED_AT, GUID1, LocalResult.NOT_FOUND, NOT_FOUND),
        CompletionCheckEvent(CREATED_AT, true),
        ConnectivitySnapshotEvent(
            createdAt = CREATED_AT,
            connections = listOf(
                Connection(
                    SimNetworkUtils.ConnectionType.MOBILE,
                    SimNetworkUtils.ConnectionState.CONNECTED,
                ),
            ),
        ),
        ConsentEvent(CREATED_AT, ENDED_AT, INDIVIDUAL, ACCEPTED),
        EnrolmentEventV2(
            createdAt = CREATED_AT,
            subjectId = GUID1,
            projectId = DEFAULT_PROJECT_ID,
            moduleId = DEFAULT_MODULE_ID,
            attendantId = DEFAULT_USER_ID,
            personCreationEventId = GUID2,
        ),
        EnrolmentEventV4(
            createdAt = CREATED_AT,
            subjectId = GUID1,
            projectId = DEFAULT_PROJECT_ID,
            moduleId = DEFAULT_MODULE_ID,
            attendantId = DEFAULT_USER_ID,
            biometricReferenceIds = listOf(GUID1, GUID2),
        ),
        GuidSelectionEvent(CREATED_AT, GUID1),
        IntentParsingEvent(CREATED_AT, COMMCARE),
        InvalidIntentEvent(CREATED_AT, "REGISTER", mapOf("extra_key" to "value")),
        OneToManyMatchEvent(
            createdAt = CREATED_AT,
            endTime = ENDED_AT,
            pool = MatchPool(PROJECT, 100),
            matcher = "MATCHER_NAME",
            result = listOf(MatchEntry(GUID1, 0F)),
            probeBiometricReferenceId = GUID1,
        ),
        OneToOneMatchEvent(
            createdAt = CREATED_AT,
            endTime = ENDED_AT,
            candidateId = GUID1,
            matcher = "MATCHER_NAME",
            result = MatchEntry(GUID1, 0F),
            fingerComparisonStrategy = FingerComparisonStrategy.SAME_FINGER,
            probeBiometricReferenceId = GUID1,
        ),
        PersonCreationEvent(
            startTime = CREATED_AT,
            fingerprintCaptureIds = listOf(GUID1),
            fingerprintReferenceId = GUID1,
            faceCaptureIds = listOf(GUID2),
            faceReferenceId = GUID2,
        ),
        RefusalEvent(CREATED_AT, ENDED_AT, OTHER, "other_text"),
        ScannerConnectionEvent(
            createdAt = CREATED_AT,
            scannerInfo = ScannerInfo(
                scannerId = "scanner_id",
                macAddress = "mac_address",
                generation = ScannerGeneration.VERO_1,
                hardwareVersion = "hardware_version",
            ),
        ),
        ScannerFirmwareUpdateEvent(CREATED_AT, ENDED_AT, "chip", "v1", "error"),
        SuspiciousIntentEvent(CREATED_AT, mapOf("extra_key" to "value")),
    )
}

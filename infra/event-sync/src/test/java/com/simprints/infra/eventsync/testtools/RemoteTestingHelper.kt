package com.simprints.infra.eventsync.testtools

import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType

internal class RemoteTestingHelper {
    // Never invoked, but used to enforce that every test class has a test
    fun enforceThatAnyTestHasATest(type: ApiEventPayloadType?) {
        when (type) {
            ApiEventPayloadType.Callout, ApiEventPayloadType.Callback,
            ApiEventPayloadType.Authentication, ApiEventPayloadType.Consent, ApiEventPayloadType.Enrolment,
            ApiEventPayloadType.Authorization, ApiEventPayloadType.FingerprintCapture, ApiEventPayloadType.OneToOneMatch,
            ApiEventPayloadType.OneToManyMatch, ApiEventPayloadType.PersonCreation, ApiEventPayloadType.AlertScreen,
            ApiEventPayloadType.GuidSelection, ApiEventPayloadType.ConnectivitySnapshot, ApiEventPayloadType.Refusal,
            ApiEventPayloadType.CandidateRead, ApiEventPayloadType.ScannerConnection, ApiEventPayloadType.Vero2InfoSnapshot,
            ApiEventPayloadType.ScannerFirmwareUpdate, ApiEventPayloadType.InvalidIntent, ApiEventPayloadType.SuspiciousIntent,
            ApiEventPayloadType.IntentParsing, ApiEventPayloadType.CompletionCheck, ApiEventPayloadType.FaceOnboardingComplete,
            ApiEventPayloadType.FaceFallbackCapture, ApiEventPayloadType.FaceCapture, ApiEventPayloadType.FaceCaptureConfirmation,
            ApiEventPayloadType.FingerprintCaptureBiometrics, ApiEventPayloadType.FaceCaptureBiometrics,
            ApiEventPayloadType.EventDownSyncRequest, ApiEventPayloadType.EventUpSyncRequest, ApiEventPayloadType.LicenseCheck,
            ApiEventPayloadType.AgeGroupSelection, ApiEventPayloadType.BiometricReferenceCreation,
            null,
            -> {
                // ADD TEST FOR NEW EVENT IN THIS CLASS
            }
        }
    }
}

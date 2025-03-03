package com.simprints.infra.eventsync.event.usecases

import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.StringTokenizer
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.events.sampledata.createAgeGroupSelectionEvent
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.infra.events.sampledata.createAuthenticationEvent
import com.simprints.infra.events.sampledata.createAuthorizationEvent
import com.simprints.infra.events.sampledata.createBiometricReferenceCreationEvent
import com.simprints.infra.events.sampledata.createCandidateReadEvent
import com.simprints.infra.events.sampledata.createCompletionCheckEvent
import com.simprints.infra.events.sampledata.createConfirmationCallbackEvent
import com.simprints.infra.events.sampledata.createConfirmationCalloutEvent
import com.simprints.infra.events.sampledata.createConnectivitySnapshotEvent
import com.simprints.infra.events.sampledata.createConsentEvent
import com.simprints.infra.events.sampledata.createEnrolmentCallbackEvent
import com.simprints.infra.events.sampledata.createEnrolmentCalloutEvent
import com.simprints.infra.events.sampledata.createEnrolmentEventV2
import com.simprints.infra.events.sampledata.createEnrolmentEventV4
import com.simprints.infra.events.sampledata.createEventDownSyncRequestEvent
import com.simprints.infra.events.sampledata.createEventUpSyncRequestEvent
import com.simprints.infra.events.sampledata.createFaceCaptureBiometricsEvent
import com.simprints.infra.events.sampledata.createFaceCaptureConfirmationEvent
import com.simprints.infra.events.sampledata.createFaceCaptureEvent
import com.simprints.infra.events.sampledata.createFaceFallbackCaptureEvent
import com.simprints.infra.events.sampledata.createFaceOnboardingCompleteEvent
import com.simprints.infra.events.sampledata.createFingerprintCaptureBiometricsEvent
import com.simprints.infra.events.sampledata.createFingerprintCaptureEvent
import com.simprints.infra.events.sampledata.createGuidSelectionEvent
import com.simprints.infra.events.sampledata.createIdentificationCallbackEvent
import com.simprints.infra.events.sampledata.createIdentificationCalloutEvent
import com.simprints.infra.events.sampledata.createIntentParsingEvent
import com.simprints.infra.events.sampledata.createInvalidIntentEvent
import com.simprints.infra.events.sampledata.createLastBiometricsEnrolmentCalloutEvent
import com.simprints.infra.events.sampledata.createLicenseCheckEvent
import com.simprints.infra.events.sampledata.createOneToManyMatchEvent
import com.simprints.infra.events.sampledata.createOneToOneMatchEvent
import com.simprints.infra.events.sampledata.createPersonCreationEvent
import com.simprints.infra.events.sampledata.createRefusalCallbackEvent
import com.simprints.infra.events.sampledata.createRefusalEvent
import com.simprints.infra.events.sampledata.createScannerConnectionEvent
import com.simprints.infra.events.sampledata.createScannerFirmwareUpdateEvent
import com.simprints.infra.events.sampledata.createSuspiciousIntentEvent
import com.simprints.infra.events.sampledata.createVerificationCallbackEventV1
import com.simprints.infra.events.sampledata.createVerificationCallbackEventV2
import com.simprints.infra.events.sampledata.createVerificationCalloutEvent
import com.simprints.infra.events.sampledata.createVero2InfoSnapshotEvent
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.AgeGroupSelection
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.AlertScreen
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Authentication
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Authorization
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.BiometricReferenceCreation
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Callback
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Callout
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.CandidateRead
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.CompletionCheck
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.ConnectivitySnapshot
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Consent
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Enrolment
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.EventDownSyncRequest
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.EventUpSyncRequest
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.FaceCapture
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.FaceCaptureBiometrics
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.FaceCaptureConfirmation
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.FaceFallbackCapture
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.FaceOnboardingComplete
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.FingerprintCapture
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.FingerprintCaptureBiometrics
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.GuidSelection
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.IntentParsing
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.InvalidIntent
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.LicenseCheck
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.OneToManyMatch
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.OneToOneMatch
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.PersonCreation
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Refusal
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.ScannerConnection
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.ScannerFirmwareUpdate
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.SuspiciousIntent
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Vero2InfoSnapshot
import com.simprints.infra.eventsync.event.validateAgeGroupSelectionEventApiModel
import com.simprints.infra.eventsync.event.validateAlertScreenEventApiModel
import com.simprints.infra.eventsync.event.validateAuthenticationEventApiModel
import com.simprints.infra.eventsync.event.validateAuthorizationEventApiModel
import com.simprints.infra.eventsync.event.validateBiometricReferenceCreationEventApiModel
import com.simprints.infra.eventsync.event.validateCallbackV1EventApiModel
import com.simprints.infra.eventsync.event.validateCallbackV2EventApiModel
import com.simprints.infra.eventsync.event.validateCalloutEventApiModel
import com.simprints.infra.eventsync.event.validateCandidateReadEventApiModel
import com.simprints.infra.eventsync.event.validateCommonParams
import com.simprints.infra.eventsync.event.validateCompletionCheckEventApiModel
import com.simprints.infra.eventsync.event.validateConnectivitySnapshotEventApiModel
import com.simprints.infra.eventsync.event.validateConsentEventApiModel
import com.simprints.infra.eventsync.event.validateDownSyncRequestEventApiModel
import com.simprints.infra.eventsync.event.validateEnrolmentEventV2ApiModel
import com.simprints.infra.eventsync.event.validateEnrolmentEventV4ApiModel
import com.simprints.infra.eventsync.event.validateFaceCaptureBiometricsEventApiModel
import com.simprints.infra.eventsync.event.validateFaceCaptureConfirmationEventApiModel
import com.simprints.infra.eventsync.event.validateFaceCaptureEventApiModel
import com.simprints.infra.eventsync.event.validateFaceFallbackCaptureEventApiModel
import com.simprints.infra.eventsync.event.validateFaceOnboardingCompleteEventApiModel
import com.simprints.infra.eventsync.event.validateFingerprintCaptureBiometricsEventApiModel
import com.simprints.infra.eventsync.event.validateFingerprintCaptureEventApiModel
import com.simprints.infra.eventsync.event.validateGuidSelectionEventApiModel
import com.simprints.infra.eventsync.event.validateIntentParsingEventApiModel
import com.simprints.infra.eventsync.event.validateInvalidEventApiModel
import com.simprints.infra.eventsync.event.validateOneToManyMatchEventApiModel
import com.simprints.infra.eventsync.event.validateOneToOneMatchEventApiModel
import com.simprints.infra.eventsync.event.validatePersonCreationEvent
import com.simprints.infra.eventsync.event.validateRefusalEventApiModel
import com.simprints.infra.eventsync.event.validateScannerConnectionEventApiModel
import com.simprints.infra.eventsync.event.validateScannerFirmwareUpdateEventApiModel
import com.simprints.infra.eventsync.event.validateSuspiciousIntentEventApiModel
import com.simprints.infra.eventsync.event.validateUpSyncRequestEventApiModel
import com.simprints.infra.eventsync.event.validateVero2InfoSnapshotEventApiModel
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("IMPLICIT_CAST_TO_ANY", "KotlinConstantConditions")
@RunWith(AndroidJUnit4::class)
internal class MapDomainEventToApiUseCaseTest {
    private val jackson = JsonHelper.jackson
    private val project = Project(
        id = "id",
        name = "name",
        description = "description",
        state = ProjectState.RUNNING,
        creator = "creator",
        imageBucket = "url",
        baseUrl = "baseUrl",
        tokenizationKeys = emptyMap(),
    )
    private val tokenizationProcessor = TokenizationProcessor(StringTokenizer(EncodingUtilsImplForTests))
    private val tokenizeEventPayloadFieldsUseCase = TokenizeEventPayloadFieldsUseCase(tokenizationProcessor)
    private val useCase = MapDomainEventToApiUseCase(tokenizeEventPayloadFieldsUseCase)

    @Test
    fun validate_alertScreenEventApiModel() {
        val event = createAlertScreenEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateAlertScreenEventApiModel(json)
    }

    @Test
    fun validate_IntentParsingEventApiModel() {
        val event = createIntentParsingEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateIntentParsingEventApiModel(json)
    }

    @Test
    fun validate_authenticationEventApiModel() {
        val event = createAuthenticationEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateAuthenticationEventApiModel(json)
    }

    @Test
    fun validate_authorizationEventApiModel() {
        val event = createAuthorizationEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateAuthorizationEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForVerificationApiModel() {
        val event = createVerificationCalloutEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForIdentificationApiModel() {
        val event = createIdentificationCalloutEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolLastBiometricsModel() {
        val event = createLastBiometricsEnrolmentCalloutEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForConfirmationApiModel() {
        val event = createConfirmationCalloutEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolmentApiModel() {
        val event = createEnrolmentCalloutEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForEnrolmentApiModel() {
        val event = createEnrolmentCallbackEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForErrorApiModel() {
        val event = createEnrolmentCallbackEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForIdentificationApiV2Model() {
        val event = createIdentificationCallbackEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV2EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForVerificationV1ApiModel() {
        val event = createVerificationCallbackEventV1()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForVerificationV2ApiModel() {
        val event = createVerificationCallbackEventV2()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV2EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForRefusalApiModel() {
        val event = createRefusalCallbackEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForConfirmationApiModel() {
        val event = createConfirmationCallbackEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_candidateReadEventApiModel() {
        val event = createCandidateReadEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCandidateReadEventApiModel(json)
    }

    @Test
    fun validate_connectivitySnapshotEventApiModel() {
        val event = createConnectivitySnapshotEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateConnectivitySnapshotEventApiModel(json)
    }

    @Test
    fun validate_consentEventApiModel() {
        val event = createConsentEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateConsentEventApiModel(json)
    }

    @Test
    fun validateEnrolmentV2_enrolmentEventApiModel() {
        val event = createEnrolmentEventV2()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentEventV2ApiModel(json)
    }

    @Test
    fun validateEnrolmentV4_enrolmentEventApiModel() {
        val event = createEnrolmentEventV4()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentEventV4ApiModel(json)
    }

    @Test
    fun validate_completionCheckEventApiModel() {
        val event = createCompletionCheckEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCompletionCheckEventApiModel(json)
    }

    @Test
    fun validate_fingerprintCaptureEventApiModel() {
        val event = createFingerprintCaptureEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFingerprintCaptureEventApiModel(json)
    }

    @Test
    fun validate_guidSelectionEventApiModel() {
        val event = createGuidSelectionEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateGuidSelectionEventApiModel(json)
    }

    @Test
    fun validate_oneToManyMatchEventApiModel() {
        val event = createOneToManyMatchEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateOneToManyMatchEventApiModel(json)
    }

    @Test
    fun validate_oneToOneMatchEventApiModel() {
        val event = createOneToOneMatchEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateOneToOneMatchEventApiModel(json)
    }

    @Test
    fun validate_personCreationEventApiModel() {
        val event = createPersonCreationEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validatePersonCreationEvent(json)
    }

    @Test
    fun validate_refusalEventApiModel() {
        val event = createRefusalEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateRefusalEventApiModel(json)
    }

    @Test
    fun validate_suspiciousIntentEventApiModel() {
        val event = createSuspiciousIntentEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateSuspiciousIntentEventApiModel(json)
    }

    @Test
    fun validate_scannerConnectionEventApiModel() {
        val event = createScannerConnectionEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateScannerConnectionEventApiModel(json)
    }

    @Test
    fun validate_vero2InfoSnapshotEvent() {
        val event = createVero2InfoSnapshotEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateVero2InfoSnapshotEventApiModel(json)
    }

    @Test
    fun validate_scannerFirmwareUpdateEvent() {
        val event = createScannerFirmwareUpdateEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateScannerFirmwareUpdateEventApiModel(json)
    }

    @Test
    fun validate_invalidEventApiModel() {
        val event = createInvalidIntentEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateInvalidEventApiModel(json)
    }

    @Test
    fun validate_FaceOnboardingCompleteEventApiModel() {
        val event = createFaceOnboardingCompleteEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceOnboardingCompleteEventApiModel(json)
    }

    @Test
    fun validate_FaceFallbackCaptureEventApiModel() {
        val event = createFaceFallbackCaptureEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceFallbackCaptureEventApiModel(json)
    }

    @Test
    fun validate_FaceCaptureEventApiModel() {
        val event = createFaceCaptureEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceCaptureEventApiModel(json)
    }

    @Test
    fun validate_FaceCaptureConfirmationEventApiModel() {
        val event = createFaceCaptureConfirmationEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceCaptureConfirmationEventApiModel(json)
    }

    @Test
    fun validate_FaceCaptureBiometricsEventApiModel() {
        val event = createFaceCaptureBiometricsEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceCaptureBiometricsEventApiModel(json)
    }

    @Test
    fun validate_FingerprintCaptureBiometricsEventApiModel() {
        val event = createFingerprintCaptureBiometricsEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFingerprintCaptureBiometricsEventApiModel(json)
    }

    @Test
    fun validate_DownSyncRequestEventApiModel() {
        val event = createEventDownSyncRequestEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateDownSyncRequestEventApiModel(json)
    }

    @Test
    fun validate_UpSyncRequestEventApiModel() {
        val event = createEventUpSyncRequestEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateUpSyncRequestEventApiModel(json)
    }

    @Test
    fun validate_ageGroupSelectionEventApiModel() {
        val event = createAgeGroupSelectionEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateAgeGroupSelectionEventApiModel(json)
    }

    @Test
    fun validate_biometricReferenceCreationEventApiModel() {
        val event = createBiometricReferenceCreationEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateBiometricReferenceCreationEventApiModel(json)
    }

    @Test
    fun `when event contains tokenized attendant id, then ApiEvent should contain tokenizedField`() {
        validateUserIdTokenization(attendantId = "attendantId".asTokenizableEncrypted())
    }

    @Test
    fun `when event contains raw attendant id, then tokenizedField in ApiEvent should be empty`() {
        validateUserIdTokenization(attendantId = "attendantId".asTokenizableRaw())
    }

    @Test
    fun `when event contains tokenized module id, then ApiEvent should contain tokenizedField`() {
        validateModuleIdTokenization(moduleId = "moduleId".asTokenizableEncrypted())
    }

    @Test
    fun `when event contains raw module id, then tokenizedField in ApiEvent should be empty`() {
        validateModuleIdTokenization(moduleId = "moduleId".asTokenizableRaw())
    }

    @Test
    fun validate_licenseCheckEventApiModel() {
        val event = createLicenseCheckEvent()
        val apiEvent = useCase(event, project)
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateLicenseCheckEventApiModel(json)
    }

    private fun validateLicenseCheckEventApiModel(json: JSONObject) {
        validateCommonParams(json, "LicenseCheck", 1)
        assertThat(json.getJSONObject("payload").getString("status")).isEqualTo("VALID")
    }

    private fun validateModuleIdTokenization(moduleId: TokenizableString) {
        val event = createEnrolmentEventV2().let {
            it.copy(payload = it.payload.copy(moduleId = moduleId))
        }
        with(useCase(event, project).tokenizedFields) {
            when (moduleId) {
                is TokenizableString.Raw -> assertThat(size).isEqualTo(0)
                is TokenizableString.Tokenized -> {
                    assertThat(first()).isEqualTo("moduleId")
                    assertThat(size).isEqualTo(1)
                }
            }
        }
    }

    private fun validateUserIdTokenization(attendantId: TokenizableString) {
        val event = createEnrolmentEventV2().let {
            it.copy(payload = it.payload.copy(attendantId = attendantId))
        }
        with(useCase(event, project).tokenizedFields) {
            when (attendantId) {
                is TokenizableString.Raw -> assertThat(size).isEqualTo(0)
                is TokenizableString.Tokenized -> {
                    assertThat(first()).isEqualTo("attendantId")
                    assertThat(size).isEqualTo(1)
                }
            }
        }
    }

    // Never invoked, but used to enforce the implementation of a test for every event event class
    fun enforceThatAnyTestHasATest() {
        val type: ApiEventPayloadType? = null
        when (type) {
            Callout -> Throwable("Callout has multiple version - there is a version for each")
            Callback -> Throwable("Callback has multiple version - there is a version for each")
            Authentication -> validate_authenticationEventApiModel()
            Consent -> validate_consentEventApiModel()
            Enrolment -> validateEnrolmentV2_enrolmentEventApiModel()
            Authorization -> validate_authorizationEventApiModel()
            FingerprintCapture -> validate_fingerprintCaptureEventApiModel()
            OneToOneMatch -> validate_oneToOneMatchEventApiModel()
            OneToManyMatch -> validate_oneToManyMatchEventApiModel()
            PersonCreation -> validate_personCreationEventApiModel()
            AlertScreen -> validate_alertScreenEventApiModel()
            GuidSelection -> validate_guidSelectionEventApiModel()
            ConnectivitySnapshot -> validate_connectivitySnapshotEventApiModel()
            Refusal -> validate_refusalEventApiModel()
            CandidateRead -> validate_candidateReadEventApiModel()
            ScannerConnection -> validate_scannerConnectionEventApiModel()
            Vero2InfoSnapshot -> validate_vero2InfoSnapshotEvent()
            ScannerFirmwareUpdate -> validate_scannerFirmwareUpdateEvent()
            InvalidIntent -> validate_IntentParsingEventApiModel()
            SuspiciousIntent -> validate_suspiciousIntentEventApiModel()
            IntentParsing -> validate_IntentParsingEventApiModel()
            CompletionCheck -> validate_completionCheckEventApiModel()
            FaceOnboardingComplete -> validate_FaceOnboardingCompleteEventApiModel()
            FaceFallbackCapture -> validate_FaceFallbackCaptureEventApiModel()
            FaceCapture -> validate_FaceCaptureEventApiModel()
            FaceCaptureConfirmation -> validate_FaceCaptureConfirmationEventApiModel()
            FingerprintCaptureBiometrics -> validate_FingerprintCaptureBiometricsEventApiModel()
            FaceCaptureBiometrics -> validate_FaceCaptureBiometricsEventApiModel()
            EventDownSyncRequest -> validate_DownSyncRequestEventApiModel()
            EventUpSyncRequest -> validate_UpSyncRequestEventApiModel()
            LicenseCheck -> validate_licenseCheckEventApiModel()
            AgeGroupSelection -> validate_ageGroupSelectionEventApiModel()
            BiometricReferenceCreation -> validate_biometricReferenceCreationEventApiModel()
            null -> TODO()
        }.safeSealedWhens
    }
}

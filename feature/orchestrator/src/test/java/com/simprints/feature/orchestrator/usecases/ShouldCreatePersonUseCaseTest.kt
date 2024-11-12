package com.simprints.feature.orchestrator.usecases

import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.Serializable

class ShouldCreatePersonUseCaseTest {

    @MockK
    lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: ShouldCreatePersonUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = ShouldCreatePersonUseCase(eventRepository)
    }

    @Test
    fun `Returns false if no action`() = runTest {
        assertThat(
            useCase(
                actionRequest = null,
                modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
                results = listOf(
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList()))
                )
            )
        ).isFalse()
    }

    @Test
    fun `Returns true if action is Enrol`() = runTest {
        assertThat(
            useCase(
                actionRequest = ActionRequest.EnrolActionRequest(
                    actionIdentifier = ActionRequestIdentifier.fromIntentAction(""),
                    projectId = "",
                    userId = "".asTokenizableRaw(),
                    moduleId = "".asTokenizableRaw(),
                    biometricDataSource = "",
                    callerPackageName = "",
                    metadata = "",
                    unknownExtras = emptyMap(),
                ),
                modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
                results = listOf(
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList()))
                )
            )
        ).isTrue()
    }

    @Test
    fun `Returns true if action is Identify`() = runTest {
        assertThat(
            useCase(
                actionRequest = ActionRequest.IdentifyActionRequest(
                    actionIdentifier = ActionRequestIdentifier.fromIntentAction(""),
                    projectId = "",
                    userId = "".asTokenizableRaw(),
                    moduleId = "".asTokenizableRaw(),
                    biometricDataSource = "",
                    callerPackageName = "",
                    metadata = "",
                    unknownExtras = emptyMap(),
                ),
                modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
                results = listOf(
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList()))
                )
            )
        ).isTrue()
    }

    @Test
    fun `Returns true if actions is Verify`() = runTest {
        assertThat(
            useCase(
                actionRequest = ActionRequest.VerifyActionRequest(
                    actionIdentifier = ActionRequestIdentifier.fromIntentAction(""),
                    projectId = "",
                    userId = "".asTokenizableRaw(),
                    moduleId = "".asTokenizableRaw(),
                    biometricDataSource = "",
                    callerPackageName = "",
                    metadata = "",
                    verifyGuid = "",
                    unknownExtras = emptyMap(),
                ),
                modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
                results = listOf(
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList()))
                )
            )
        ).isTrue()
    }

    @Test
    fun `Returns false if followup action is ConfirmIdentity`() = runTest {
        assertThat(
            useCase(
                actionRequest = ActionRequest.ConfirmIdentityActionRequest(
                    actionIdentifier = ActionRequestIdentifier.fromIntentAction(""),
                    projectId = "",
                    userId = "".asTokenizableRaw(),
                    sessionId = "",
                    selectedGuid = "",
                    metadata = "",
                    unknownExtras = emptyMap(),
                ),
                modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
                results = listOf(
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList()))
                )
            )
        ).isFalse()
    }

    @Test
    fun `Returns true if followup action is Enrol last biometric`() = runTest {
        assertThat(
            useCase(
                actionRequest = ActionRequest.EnrolLastBiometricActionRequest(
                    actionIdentifier = ActionRequestIdentifier.fromIntentAction(""),
                    projectId = "",
                    userId = "".asTokenizableRaw(),
                    moduleId = "".asTokenizableRaw(),
                    metadata = "",
                    sessionId = "",
                    unknownExtras = emptyMap(),
                ),
                modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
                results = listOf(
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList()))
                )
            )
        ).isTrue()
    }

    @Test
    fun `Returns false if no modalities`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = emptySet(),
                results = listOf(
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList()))
                )
            )
        ).isFalse()
    }

    @Test
    fun `Returns false when only fingerprint required and no results`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
                results = listOf(createStep(StepId.FINGERPRINT_CAPTURE, null))
            )
        ).isFalse()
    }

    @Test
    fun `Returns false when only face required and no results`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(GeneralConfiguration.Modality.FACE),
                results = listOf(createStep(StepId.FACE_CAPTURE, null))
            )
        ).isFalse()
    }

    @Test
    fun `Returns false when both modalities required but there are no capture results`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(
                    GeneralConfiguration.Modality.FACE,
                    GeneralConfiguration.Modality.FINGERPRINT
                ),
                results = emptyList()
            )
        ).isFalse()
    }

    @Test
    fun `Returns true when only fingerprint required and provided`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
                results = listOf(
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList()))
                )
            )
        ).isTrue()
    }

    @Test
    fun `Returns false when both modalities required and face result missing`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(
                    GeneralConfiguration.Modality.FACE,
                    GeneralConfiguration.Modality.FINGERPRINT
                ),
                results = listOf(
                    createStep(StepId.FACE_CAPTURE, null),
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList())),
                )
            )
        ).isFalse()
    }

    @Test
    fun `Returns false when both modalities required and fingerprint result missing`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(
                    GeneralConfiguration.Modality.FACE,
                    GeneralConfiguration.Modality.FINGERPRINT
                ),
                results = listOf(
                    createStep(StepId.FACE_CAPTURE, FaceCaptureResult(emptyList())),
                    createStep(StepId.FINGERPRINT_CAPTURE, null),
                )
            )
        ).isFalse()
    }

    @Test
    fun `Returns true when only face required and provided`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(GeneralConfiguration.Modality.FACE),
                results = listOf(createStep(StepId.FACE_CAPTURE, FaceCaptureResult(emptyList())))
            )
        ).isTrue()
    }

    @Test
    fun `Returns true when both modalities required and both results provided`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(
                    GeneralConfiguration.Modality.FACE,
                    GeneralConfiguration.Modality.FINGERPRINT
                ),
                results = listOf(
                    createStep(StepId.FACE_CAPTURE, FaceCaptureResult(emptyList())),
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList())),
                )
            )
        ).isTrue()
    }

    @Test
    fun `Returns true when there is a PersonCreationEvent but it's missing a face reference that was scheduled for capture`() = runTest {
        val faceCaptureResults = listOf(createStep(StepId.FACE_CAPTURE, FaceCaptureResult(emptyList())))

        val sessionEvents = listOf(
            PersonCreationEvent(
                startTime = Timestamp(0),
                fingerprintCaptureIds = listOf("1", "2"),
                fingerprintReferenceId = "123",
                faceCaptureIds = null,
                faceReferenceId = null,
            )
        )
        coEvery { eventRepository.getEventsInCurrentSession() } returns sessionEvents

        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(
                    GeneralConfiguration.Modality.FACE,
                    GeneralConfiguration.Modality.FINGERPRINT
                ),
                results = faceCaptureResults
            )
        ).isTrue()
    }

    @Test
    fun `Returns true when there is a PersonCreationEvent but it's missing a fingerprint reference that was scheduled for capture`() = runTest {
        val fingerprintCaptureResults = listOf(createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList())))

        val sessionEvents = listOf(
            PersonCreationEvent(
                startTime = Timestamp(0),
                fingerprintCaptureIds = null,
                fingerprintReferenceId = null,
                faceCaptureIds = listOf("1", "2"),
                faceReferenceId = "123",
            )
        )
        coEvery { eventRepository.getEventsInCurrentSession() } returns sessionEvents

        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(
                    GeneralConfiguration.Modality.FACE,
                    GeneralConfiguration.Modality.FINGERPRINT
                ),
                results = fingerprintCaptureResults
            )
        ).isTrue()
    }

    @Test
    fun `Returns false when there are already 2 PersonCreation events in the session`() = runTest {
        val sessionEvents = listOf(
            PersonCreationEvent(
                startTime = Timestamp(0),
                fingerprintCaptureIds = null,
                fingerprintReferenceId = null,
                faceCaptureIds = listOf("1", "2"),
                faceReferenceId = "123",
            ),
            PersonCreationEvent(
                startTime = Timestamp(0),
                fingerprintCaptureIds = listOf("1", "2"),
                fingerprintReferenceId = "123",
                faceCaptureIds = listOf("1", "2"),
                faceReferenceId = "123",
            )
        )
        coEvery { eventRepository.getEventsInCurrentSession() } returns sessionEvents

        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = setOf(
                    GeneralConfiguration.Modality.FACE,
                    GeneralConfiguration.Modality.FINGERPRINT
                ),
                results = listOf(
                    createStep(StepId.FACE_CAPTURE, FaceCaptureResult(emptyList())),
                    createStep(StepId.FINGERPRINT_CAPTURE, FingerprintCaptureResult(emptyList())),
                )
            )
        ).isFalse()
    }

    private val flowAction = ActionRequest.EnrolActionRequest(
        actionIdentifier = ActionRequestIdentifier.fromIntentAction(""),
        projectId = "",
        userId = "".asTokenizableRaw(),
        moduleId = "".asTokenizableRaw(),
        biometricDataSource = "",
        callerPackageName = "",
        metadata = "",
        unknownExtras = emptyMap(),
    )

    private fun createStep(id: Int, result: Serializable?) = Step(
        id = id,
        navigationActionId = 1,
        destinationId = 1,
        payload = Bundle(),
        status = StepStatus.COMPLETED,
        result = result
    )
}

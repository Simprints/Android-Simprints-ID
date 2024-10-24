package com.simprints.feature.orchestrator.usecases

import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import io.mockk.MockKAnnotations
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
                modalities = emptySet(),
                results = emptyList()
            )
        ).isFalse()
    }

    @Test
    fun `Returns false if followup action`() = runTest {
        assertThat(
            useCase(
                actionRequest = ActionRequest.ConfirmIdentityActionRequest(
                    actionIdentifier = ActionRequestIdentifier.fromIntentAction(""),
                    projectId = "",
                    userId = "".asTokenizableRaw(),
                    sessionId = "",
                    selectedGuid = "",
                    unknownExtras = emptyMap(),
                ), modalities = emptySet(), results = emptyList()
            )
        ).isFalse()
    }

    @Test
    fun `Returns false if no modalities`() = runTest {
        assertThat(
            useCase(
                actionRequest = flowAction,
                modalities = emptySet(),
                results = emptyList()
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

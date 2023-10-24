package com.simprints.feature.orchestrator.usecases

import android.os.Bundle
import android.os.Parcelable
import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ShouldCreatePersonUseCaseTest {

    private lateinit var useCase: ShouldCreatePersonUseCase

    @Before
    fun setUp() {
        useCase = ShouldCreatePersonUseCase()
    }

    @Test
    fun `Returns false if no action`() {
        assertThat(useCase(
            actionRequest = null,
            modalities = emptySet(),
            results = emptyList()
        )).isFalse()
    }

    @Test
    fun `Returns false if followup action`() {
        assertThat(useCase(
            actionRequest = ActionRequest.ConfirmIdentityActionRequest(
                ActionRequestIdentifier.fromIntentAction(""),
                "", "", "", "", emptyList(),
            ),
            modalities = emptySet(),
            results = emptyList()
        )).isFalse()
    }

    @Test
    fun `Returns false if no modalities`() {
        assertThat(useCase(
            actionRequest = flowAction,
            modalities = emptySet(),
            results = emptyList()
        )).isFalse()
    }

    @Ignore("Enable when fingerprint modality is refactored")
    @Test
    fun `Returns false when only fingerprint required and no results`() {
        assertThat(useCase(
            actionRequest = flowAction,
            modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
            results = listOf(createStep(StepId.FINGERPRINT_CAPTURE, null))
        )).isFalse()
    }

    @Test
    fun `Returns false when only face required and no results`() {
        assertThat(useCase(
            actionRequest = flowAction,
            modalities = setOf(GeneralConfiguration.Modality.FACE),
            results = listOf(createStep(StepId.FACE_CAPTURE, null))
        )).isFalse()
    }

    @Ignore("Enable when fingerprint modality is refactored")
    @Test
    fun `Returns true when only fingerprint required and provided`() {
        assertThat(useCase(
            actionRequest = flowAction,
            modalities = setOf(GeneralConfiguration.Modality.FINGERPRINT),
            // TODO change to FingerprintCaptureResult
            results = listOf(createStep(StepId.FINGERPRINT_CAPTURE, FaceCaptureResult(emptyList())))
        )).isTrue()
    }

    @Ignore("Enable when fingerprint modality is refactored")
    @Test
    fun `Returns false when both modalities required and face result missing`() {
        assertThat(useCase(
            actionRequest = flowAction,
            modalities = setOf(GeneralConfiguration.Modality.FACE, GeneralConfiguration.Modality.FINGERPRINT),
            results = listOf(
                createStep(StepId.FACE_CAPTURE, null),
                // TODO change to FingerprintCaptureResult
                createStep(StepId.FINGERPRINT_CAPTURE, FaceCaptureResult(emptyList())),
            )
        )).isFalse()
    }

    @Ignore("Enable when fingerprint modality is refactored")
    @Test
    fun `Returns false when both modalities required and fingerprint result missing`() {
        assertThat(useCase(
            actionRequest = flowAction,
            modalities = setOf(GeneralConfiguration.Modality.FACE, GeneralConfiguration.Modality.FINGERPRINT),
            results = listOf(
                createStep(StepId.FACE_CAPTURE, FaceCaptureResult(emptyList())),
                createStep(StepId.FINGERPRINT_CAPTURE, null),
            )
        )).isFalse()
    }

    @Test
    fun `Returns true when only face required and provided`() {
        assertThat(useCase(
            actionRequest = flowAction,
            modalities = setOf(GeneralConfiguration.Modality.FACE),
            results = listOf(createStep(StepId.FACE_CAPTURE, FaceCaptureResult(emptyList())))
        )).isTrue()
    }

    @Ignore("Enable when fingerprint modality is refactored")
    @Test
    fun `Returns true when both modalities required and both results provided`() {
        assertThat(useCase(
            actionRequest = flowAction,
            modalities = setOf(GeneralConfiguration.Modality.FACE, GeneralConfiguration.Modality.FINGERPRINT),
            results = listOf(
                createStep(StepId.FACE_CAPTURE, FaceCaptureResult(emptyList())),
                // TODO change to FingerprintCaptureResult
                createStep(StepId.FINGERPRINT_CAPTURE, FaceCaptureResult(emptyList())),
            )
        )).isTrue()
    }

    private val flowAction = ActionRequest.EnrolActionRequest(
        ActionRequestIdentifier.fromIntentAction(""),
        "", "", "", "", emptyList(),
    )

    private fun createStep(id: Int, result: Parcelable?) = Step(
        id = id,
        navigationActionId = 1,
        destinationId = 1,
        payload = Bundle(),
        status = StepStatus.COMPLETED,
        result = result
    )
}

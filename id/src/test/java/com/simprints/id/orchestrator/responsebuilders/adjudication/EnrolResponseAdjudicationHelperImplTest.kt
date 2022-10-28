package com.simprints.id.orchestrator.responsebuilders.adjudication

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.ProjectConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class EnrolResponseAdjudicationHelperImplTest {

    companion object {
        private val fingerprintConfidenceDecisionPolicy = DecisionPolicy(15, 30, 40)
        private val faceConfidenceDecisionPolicy = DecisionPolicy(15, 30, 40)
    }

    private val isEnrolmentPlus = true
    private val projectConfiguration = mockk<ProjectConfiguration> {
        every { general } returns mockk {
            every { duplicateBiometricEnrolmentCheck } returns isEnrolmentPlus
        }
        every { face } returns mockk {
            every { decisionPolicy } returns faceConfidenceDecisionPolicy
        }
        every { fingerprint } returns mockk {
            every { decisionPolicy } returns fingerprintConfidenceDecisionPolicy
        }
    }
    private val enrolResponseAdjudicationHelper: EnrolResponseAdjudicationHelper =
        EnrolResponseAdjudicationHelperImpl()

    @Test
    fun getAdjudicationForFingerprintConfidenceScoreBelowMedium_shouldReturnEnrolAction() {
        val lowerThanMediumConfidenceScore = 29f
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(lowerThanMediumConfidenceScore)
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.ENROL)
    }

    @Test
    fun getAdjudicationForFingerprintConfidenceScoreAboveMedium_shouldReturnIdentifyAction() {
        val mediumConfidenceScore = 30f
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(mediumConfidenceScore)
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.IDENTIFY)
    }


    @Test
    fun getAdjudicationForFaceConfidenceScoreBelowMedium_shouldReturnEnrolAction() {
        val lowerThanMediumConfidenceScore = 29f
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFaceMatchStepsWithConfidenceScore(lowerThanMediumConfidenceScore)
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.ENROL)
    }

    @Test
    fun getAdjudicationForFaceConfidenceScoreAboveMedium_shouldReturnIdentifyAction() {
        val mediumConfidenceScore = 30f
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFaceMatchStepsWithConfidenceScore(mediumConfidenceScore)
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.IDENTIFY)
    }

    private fun buildFingerprintMatchStepsWithMatchScore(confidenceScore: Float) = listOf(
        Step(
            requestCode = 234,
            activityName = "com.simprints.id.MyFingerprintActivity",
            bundleKey = "BUNDLE_KEY",
            request = mockk(),
            result = FingerprintMatchResponse(
                listOf(
                    FingerprintMatchResult("person_id", confidenceScore)
                )
            ),
            status = Step.Status.COMPLETED
        )
    )

    private fun buildFaceMatchStepsWithConfidenceScore(confidenceScore: Float) = listOf(
        Step(
            requestCode = 322,
            activityName = "com.simprints.id.MyFaceActivity",
            bundleKey = "BUNDLE_KEY",
            request = mockk(),
            result = FaceMatchResponse(
                listOf(
                    FaceMatchResult(guidFound = "person_id", confidence = confidenceScore)
                )
            ),
            status = Step.Status.COMPLETED
        )
    )

}

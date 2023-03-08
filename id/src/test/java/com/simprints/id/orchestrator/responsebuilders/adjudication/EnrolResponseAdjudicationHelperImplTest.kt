package com.simprints.id.orchestrator.responsebuilders.adjudication

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.ProjectConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class EnrolResponseAdjudicationHelperImplTest {

    companion object {
        private const val mediumConfidenceScore = 30
        private const val lowerThanMediumConfidenceScore = mediumConfidenceScore - 1
        private val fingerprintConfidenceDecisionPolicy = DecisionPolicy(15, mediumConfidenceScore, 40)
        private val faceConfidenceDecisionPolicy = DecisionPolicy(15, mediumConfidenceScore, 40)
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
    fun getAdjudicationForNoMatchResponse_shouldReturnEnrolAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                listOf()
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.ENROL)
    }

    @Test
    fun getAdjudicationForFingerprintConfidenceScoreBelowMedium_shouldReturnEnrolAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(lowerThanMediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.ENROL)
    }

    @Test
    fun getAdjudicationForFingerprintConfidenceScoreAboveMedium_shouldReturnIdentifyAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(mediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.IDENTIFY)
    }

    @Test
    fun getAdjudicationForFingerprintConfidenceScoreAboveMediumWithDbecOff_shouldReturnEnrolAction() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns false
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(mediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.ENROL)
    }

    @Test
    fun getAdjudicationForFaceConfidenceScoreBelowMedium_shouldReturnEnrolAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFaceMatchStepsWithConfidenceScore(lowerThanMediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.ENROL)
    }

    @Test
    fun getAdjudicationForFaceConfidenceScoreAboveMedium_shouldReturnIdentifyAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFaceMatchStepsWithConfidenceScore(mediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.IDENTIFY)
    }

    @Test
    fun getAdjudicationForFaceConfidenceScoreAboveMediumWithDbecOff_shouldReturnEnrolAction() {
        every { projectConfiguration.general.duplicateBiometricEnrolmentCheck } returns false
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFaceMatchStepsWithConfidenceScore(mediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.ENROL)
    }

    @Test
    fun getAdjudicationForFingerprintAboveAndFaceBelowMedium_shouldReturnIdentifyAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(mediumConfidenceScore.toFloat()) +
                    buildFaceMatchStepsWithConfidenceScore(lowerThanMediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.IDENTIFY)
    }

    @Test
    fun getAdjudicationForFingerprintBelowAndFaceAboveMedium_shouldReturnIdentifyAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(lowerThanMediumConfidenceScore.toFloat()) +
                    buildFaceMatchStepsWithConfidenceScore(mediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.IDENTIFY)
    }

    @Test
    fun getAdjudicationForFingerprintAndFaceAboveMedium_shouldReturnIdentifyAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(mediumConfidenceScore.toFloat()) +
                    buildFaceMatchStepsWithConfidenceScore(mediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.IDENTIFY)
    }

    @Test
    fun getAdjudicationForFingerprintAndFaceBelowMedium_shouldReturnEnrolAction() {
        val adjudication =
            enrolResponseAdjudicationHelper.getAdjudicationAction(
                projectConfiguration,
                buildFingerprintMatchStepsWithMatchScore(lowerThanMediumConfidenceScore.toFloat()) +
                    buildFaceMatchStepsWithConfidenceScore(lowerThanMediumConfidenceScore.toFloat())
            )

        assertThat(adjudication).isEqualTo(EnrolAdjudicationAction.ENROL)
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

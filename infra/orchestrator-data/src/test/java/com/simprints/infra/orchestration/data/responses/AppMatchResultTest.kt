package com.simprints.infra.orchestration.data.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.domain.response.AppResponseTier
import com.simprints.infra.config.store.models.DecisionPolicy
import org.junit.Test


class AppMatchResultTest {

    @Test
    fun testAppMatchResultConstructor() {
        assertThat(
            AppMatchResult(
                guid = "guid",
                confidenceScore = 25.0f,
                decisionPolicy = DecisionPolicy(low = 10, medium = 20, high = 30)
            )
        ).isEqualTo(
            AppMatchResult(
                guid = "guid",
                confidenceScore = 25,
                tier = AppResponseTier.TIER_4,
                matchConfidence = AppMatchConfidence.MEDIUM,
            )
        )
    }

    @Test
    fun testComputeTier() {
        mapOf(
            0.0f to AppResponseTier.TIER_5,
            10.0f to AppResponseTier.TIER_5,
            20.0f to AppResponseTier.TIER_4,
            30.0f to AppResponseTier.TIER_4,
            35.0f to AppResponseTier.TIER_3,
            40.0f to AppResponseTier.TIER_3,
            50.0f to AppResponseTier.TIER_2,
            60.0f to AppResponseTier.TIER_2,
            70.0f to AppResponseTier.TIER_2,
            75.0f to AppResponseTier.TIER_1,
            80.0f to AppResponseTier.TIER_1,
            90.0f to AppResponseTier.TIER_1,
        ).forEach { (score, expected) ->
            assertThat(AppMatchResult.computeTier(score)).isEqualTo(expected)
        }
    }

    @Test
    fun testComputeMatchConfidence() {
        val policy = DecisionPolicy(low = 10, medium = 20, high = 30)
        mapOf(
            0 to AppMatchConfidence.NONE,
            10 to AppMatchConfidence.LOW,
            15 to AppMatchConfidence.LOW,
            20 to AppMatchConfidence.MEDIUM,
            25 to AppMatchConfidence.MEDIUM,
            30 to AppMatchConfidence.HIGH,
            35 to AppMatchConfidence.HIGH,
        ).forEach { (score, expected) ->
            assertThat(AppMatchResult.computeMatchConfidence(score, policy)).isEqualTo(expected)
        }
    }

    @Test
    fun `test computeVerificationSuccess with verificationMatchThreshold present`() {
        mapOf(
            0 to false,
            10 to false,
            20 to false,
            30 to false,
            35 to false,
            40 to true,
            50 to true,
            60 to true,
            70 to true,
            75 to true,
            80 to true,
            90 to true,
        ).forEach { (score, expected) ->
            assertThat(AppMatchResult.computeVerificationSuccess(score, 40.0f)).isEqualTo(expected)
        }
    }

    @Test
    fun `test computeVerificationSuccess with verificationMatchThreshold null`() {
        mapOf(
            0 to null,
            10 to null,
            20 to null,
            30 to null,
            35 to null,
            40 to null,
            50 to null,
            60 to null,
            70 to null,
            75 to null,
            80 to null,
            90 to null,
        ).forEach { (score, expected) ->
            assertThat(AppMatchResult.computeVerificationSuccess(score, null)).isEqualTo(expected)
        }
    }
}

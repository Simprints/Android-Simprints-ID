package com.simprints.infra.orchestration.data.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.domain.response.AppResponseTier
import com.simprints.infra.config.store.models.DecisionPolicy
import org.junit.Test


class AppMatchResultTest {

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
        val policy = DecisionPolicy(low = 10, medium = 20, high = 30,)
        mapOf(
            0 to AppMatchConfidence.NONE,
            10 to AppMatchConfidence.LOW,
            15 to AppMatchConfidence.LOW,
            20 to AppMatchConfidence.MEDIUM,
            25 to AppMatchConfidence.MEDIUM,
            30 to AppMatchConfidence.HIGH,
            35 to AppMatchConfidence.HIGH,
        ).forEach { (score, expected) ->
            assertThat(AppMatchResult.computeMatchConfidence(score, policy)).isEqualTo(expected)}
    }
}

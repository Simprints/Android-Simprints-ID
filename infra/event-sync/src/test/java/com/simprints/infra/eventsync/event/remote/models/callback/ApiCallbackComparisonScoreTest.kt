package com.simprints.infra.eventsync.event.remote.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.callback.CallbackComparisonScore
import com.simprints.infra.eventsync.event.remote.models.ApiConfidenceMatch
import com.simprints.infra.eventsync.event.remote.models.ApiTier
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppResponseTier
import org.junit.Test

class ApiCallbackComparisonScoreTest {

    @Test
    fun `correctly converts domain to api v1`() {
        val apiModel = CallbackComparisonScore(
            guid = "guid",
            confidence = 1,
            tier = IAppResponseTier.TIER_1,
            confidenceMatch = IAppMatchConfidence.HIGH,
        ).fromDomainToApi(1) as ApiCallbackComparisonScore.ApiCallbackComparisonScoreV1

        assertThat(apiModel.tier).isEqualTo(ApiTier.TIER_1)
    }

    @Test
    fun `correctly converts domain to api v2`() {
        val apiModel = CallbackComparisonScore(
            guid = "guid",
            confidence = 1,
            tier = IAppResponseTier.TIER_1,
            confidenceMatch = IAppMatchConfidence.HIGH,
        ).fromDomainToApi(2) as ApiCallbackComparisonScore.ApiCallbackComparisonScoreV2

        assertThat(apiModel.tier).isEqualTo(ApiTier.TIER_1)
        assertThat(apiModel.confidenceMatch).isEqualTo(ApiConfidenceMatch.HIGH)
    }
}

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
    fun `correctly converts domain to api`() {
        val apiModel = CallbackComparisonScore(
            guid = "guid",
            confidence = 1,
            tier = IAppResponseTier.TIER_1,
            confidenceMatch = IAppMatchConfidence.HIGH,
        ).fromDomainToApi()

        assertThat(apiModel.tier).isEqualTo(ApiTier.TIER_1)
        assertThat(apiModel.confidenceMatch).isEqualTo(ApiConfidenceMatch.HIGH)
    }

    @Test
    fun `correctly converts api to domain`() {
        val domainModel = ApiCallbackComparisonScore(
            guid = "guid",
            confidence = 1,
            tier = ApiTier.TIER_1,
            confidenceMatch = ApiConfidenceMatch.HIGH,
        ).fromApiToDomain()

        assertThat(domainModel.tier).isEqualTo(IAppResponseTier.TIER_1)
        assertThat(domainModel.confidenceMatch).isEqualTo(IAppMatchConfidence.HIGH)
    }

    @Test
    fun `correctly converts domain to api with missing confidence match`() {
        val apiModel = CallbackComparisonScore(
            guid = "guid",
            confidence = 1,
            tier = IAppResponseTier.TIER_1,
            confidenceMatch = null,
        ).fromDomainToApi()

        assertThat(apiModel.tier).isEqualTo(ApiTier.TIER_1)
        assertThat(apiModel.confidenceMatch).isEqualTo(ApiConfidenceMatch.NONE)
    }

    @Test
    fun `correctly converts api to domain with missing confidence mathc`() {
        val domainModel = ApiCallbackComparisonScore(
            guid = "guid",
            confidence = 1,
            tier = ApiTier.TIER_1,
            confidenceMatch = null,
        ).fromApiToDomain()

        assertThat(domainModel.tier).isEqualTo(IAppResponseTier.TIER_1)
        assertThat(domainModel.confidenceMatch).isEqualTo(IAppMatchConfidence.NONE)
    }
}

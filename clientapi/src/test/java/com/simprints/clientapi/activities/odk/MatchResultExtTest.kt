package com.simprints.clientapi.activities.odk

import com.google.common.truth.Truth
import com.simprints.clientapi.domain.responses.entities.MatchConfidence
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import org.junit.Test

class MatchResultExtTest {

    @Test
    fun matchResultShouldBeTransformedToOdkFormat(){
        val listId = listOf(MatchResult("some_guid", 40, Tier.TIER_1, MatchConfidence.HIGH), MatchResult("some_guid2", 25, Tier.TIER_2, MatchConfidence.MEDIUM))
        val okdIdsFormat = listId.getIdsString()
        val okdConfidenceFormat = listId.getConfidencesString()
        val okdTierFormat = listId.getTiersString()


        Truth.assertThat(okdIdsFormat).isEqualTo("some_guid some_guid2")
        Truth.assertThat(okdConfidenceFormat).isEqualTo("40 25")
        Truth.assertThat(okdTierFormat).isEqualTo("TIER_1 TIER_2")
    }
}

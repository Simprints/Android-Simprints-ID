package com.simprints.clientapi.activities.odk

import com.google.common.truth.Truth
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import org.junit.Test

class MatchResultExtTest {

    @Test
    fun matchResultShouldBeTransformedToOdkFormat(){
        val listId = listOf(MatchResult("some_guid", 90, Tier.TIER_1), MatchResult("some_guid2", 90, Tier.TIER_2))
        val okdIdsFormat = listId.getIdsString()
        val okdConfidenceFormat = listId.getConfidencesString()
        val okdTierFormat = listId.getTiersString()


        Truth.assertThat(okdIdsFormat).isEqualTo("some_guid some_guid2")
        Truth.assertThat(okdConfidenceFormat).isEqualTo("90 90")
        Truth.assertThat(okdTierFormat).isEqualTo("TIER_1 TIER_2")
    }
}

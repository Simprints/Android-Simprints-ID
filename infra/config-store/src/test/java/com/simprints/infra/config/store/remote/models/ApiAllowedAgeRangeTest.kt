package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.AgeGroup
import org.junit.Test

class ApiAllowedAgeRangeTest {
    @Test
    fun `should map correctly the model`() {
        val apiAllowedAgeRange = ApiAllowedAgeRange(10, 20)
        val expectedAgeGroup = AgeGroup(10, 20)

        assertThat(apiAllowedAgeRange.toDomain()).isEqualTo(expectedAgeGroup)
    }

    @Test
    fun `should return empty range when startInclusive and endExclusive are null`() {
        val apiAllowedAgeRange = ApiAllowedAgeRange(null, null)
        val expectedAgeGroup = AgeGroup(0, null)

        assertThat(apiAllowedAgeRange.toDomain()).isEqualTo(expectedAgeGroup)
    }

    @Test
    fun `should use zero as default value when startInclusive is null`() {
        val apiAllowedAgeRange = ApiAllowedAgeRange(null, 20)
        val expectedAgeGroup = AgeGroup(0, 20)

        assertThat(apiAllowedAgeRange.toDomain()).isEqualTo(expectedAgeGroup)
    }

    @Test
    fun `should map null endExclusive correctly`() {
        val apiAllowedAgeRange = ApiAllowedAgeRange(0, null)
        val expectedAgeGroup = AgeGroup(0, null)

        assertThat(apiAllowedAgeRange.toDomain()).isEqualTo(expectedAgeGroup)
    }
}

package com.simprints.feature.orchestrator.cache

import android.content.SharedPreferences
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.security.SecurityManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class OrchestratorCacheTest {

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var prefs: SharedPreferences

    @MockK
    private lateinit var jsonHelper: JsonHelper

    private lateinit var cache: OrchestratorCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns prefs

        cache = OrchestratorCache(
            securityManager,
            jsonHelper,
        )
    }

    @Test
    fun `Stores steps if passed value`() {
        val json = "[]"
        every { jsonHelper.toJson(any(), any()) } returns json

        val steps: List<Step> = listOf(mockk(), mockk())
        cache.steps = steps

        val stepsResultJson = steps.joinToString(separator = ",") { json }
        verify {
            jsonHelper.toJson(any(), any())
            prefs.edit().putString(any(), eq("[${stepsResultJson}]"))
        }
    }

    @Test
    fun `Restores steps if stored`() {
        val json = "[]"
        every { prefs.getString(any(), any()) } returns json
        every { jsonHelper.fromJson<List<Step>>(any(), any(), any()) } returns emptyList()

        val result = cache.steps

        verify {
            jsonHelper.fromJson<List<Step>>(any(), any(), any())
            prefs.getString(any(), any())
        }
    }

    @Test
    fun `Skips unmarshalling if no steps`() {
        every { prefs.getString(any(), any()) } returns null

        val result = cache.steps

        verify(exactly = 0) {
            jsonHelper.fromJson<List<String>>(any())
        }
    }

    @Test
    fun `Stores age group if passed value`() {
        val json = "[1,2]"
        every { jsonHelper.toJson(any()) } returns json

        cache.ageGroup = AgeGroup(1, 2)

        verify(exactly = 1) { prefs.edit().putString(any(), json) }
    }

    @Test
    fun `Restores age group if stored`() {
        val json = "[1,2]"
        every { prefs.getString(any(), any()) } returns json
        every { jsonHelper.fromJson<AgeGroup>(any(), any<TypeReference<AgeGroup>>()) } returns AgeGroup(1, 2)

        val result = cache.ageGroup

        verify(exactly = 1) { prefs.getString(any(), any()) }
    }

    @Test
    fun `Clears cache when requested`() {
        val result = cache.clearCache()

        verify(exactly = 1) { prefs.edit().remove("steps") }
        verify(exactly = 1) { prefs.edit().remove("age_group") }
    }

    @Test
    fun `AgeGroup is serialized by Jackson without addition of phantom attributes`() {
        // see Jackson unwanted attribute serialization bug https://stackoverflow.com/questions/69616587/why-does-jackson-add-an-empty-false-into-the-json
        val realJsonHelper = JsonHelper
        val originalAgeGroup = AgeGroup(startInclusive = 0, endExclusive = 1)

        val json = realJsonHelper.toJson(originalAgeGroup)

        assertThat(json).isEqualTo("{\"type\":\"AgeGroup\",\"startInclusive\":0,\"endExclusive\":1}")
    }

    @Test
    fun `AgeGroup is deserialized correctly by Jackson`() {
        val realJsonHelper = JsonHelper
        val originalAgeGroup = AgeGroup(startInclusive = 0, endExclusive = 1)

        val json = "{\"type\":\"AgeGroup\",\"startInclusive\":0,\"endExclusive\":1}"

        val result = realJsonHelper.fromJson(json, object : TypeReference<AgeGroup>() {})

        assertThat(result).isEqualTo(originalAgeGroup)
    }
}

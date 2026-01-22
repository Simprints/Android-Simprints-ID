package com.simprints.feature.orchestrator.cache

import android.content.SharedPreferences
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.AgeGroup
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.tools.OrchestrationJsonHelper
import com.simprints.infra.security.SecurityManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrchestratorCacheTest {
    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var prefs: SharedPreferences

    private val orchestrationJsonHelper: OrchestrationJsonHelper = OrchestrationJsonHelper()

    private lateinit var cache: OrchestratorCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { securityManager.buildEncryptedSharedPreferences(any()) } returns prefs

        cache = OrchestratorCache(
            securityManager,
            orchestrationJsonHelper,
        )
    }

    @Test
    fun `Stores steps if passed value`() {
        val steps: List<Step> = listOf(
            Step(id = StepId.SETUP, navigationActionId = 0, destinationId = 0),
            Step(id = StepId.CONSENT, navigationActionId = 0, destinationId = 0),
        )
        val stepsResultJson = orchestrationJsonHelper.encodeToString<List<Step>>(steps)
        cache.steps = steps

        verify {
            prefs.edit().putString(any(), eq(stepsResultJson))
        }
    }

    @Test
    fun `Stores empty list of steps if passed value`() {
        val steps: List<Step> = emptyList()
        val stepsResultJson = orchestrationJsonHelper.encodeToString<List<Step>>(steps)
        cache.steps = steps
        verify {
            prefs.edit().putString(any(), eq(stepsResultJson))
        }
    }

    @Test
    fun `Restores steps if stored`() {
        val stepsList = listOf(
            Step(id = StepId.SETUP, navigationActionId = 0, destinationId = 0),
            Step(id = StepId.CONSENT, navigationActionId = 0, destinationId = 0),
        )
        val jsonString = orchestrationJsonHelper.encodeToString(stepsList)

        every { prefs.getString(any(), any()) } returns jsonString

        val result = cache.steps

        assertThat(result).isEqualTo(stepsList)

        verify(exactly = 1) { prefs.getString(any(), any()) }
    }

    @Test
    fun `Skips unmarshalling if no steps`() {
        every { prefs.getString(any(), any()) } returns null

        val result = cache.steps

        assertThat(result).isEmpty()
    }

    @Test
    fun `Stores age group if passed value`() {
        val ageGroup = AgeGroup(1, 2)
        val jsonString = orchestrationJsonHelper.encodeToString(ageGroup)

        cache.ageGroup = ageGroup
        verify(exactly = 1) { prefs.edit().putString(any(), jsonString) }
    }

    @Test
    fun `Restores age group if stored`() {
        val json = orchestrationJsonHelper.encodeToString(AgeGroup(1, 2))
        every { prefs.getString(any(), any()) } returns json

        val result = cache.ageGroup

        verify(exactly = 1) { prefs.getString(any(), any()) }
        assertThat(result).isEqualTo(AgeGroup(1, 2))
    }

    @Test
    fun `Clears cache when requested`() {
        val result = cache.clearCache()

        verify(exactly = 1) { prefs.edit().remove("steps") }
        verify(exactly = 1) { prefs.edit().remove("age_group") }
        val steps = cache.steps
        assertThat(steps).isEmpty()
    }

    @Test
    fun `AgeGroup is serialized without addition of phantom attributes`() {
        val originalAgeGroup = AgeGroup(startInclusive = 0, endExclusive = 1)

        val jsonString = orchestrationJsonHelper.encodeToString(originalAgeGroup)

        assertThat(jsonString).isEqualTo("{\"startInclusive\":0,\"endExclusive\":1}")
    }

    @Test
    fun `AgeGroup is deserialized correctly `() {
        val originalAgeGroup = AgeGroup(startInclusive = 0, endExclusive = 1)
        val jsonString = "{\"type\":\"AgeGroup\",\"startInclusive\":0,\"endExclusive\":1}"
        val result = orchestrationJsonHelper.decodeFromString<AgeGroup>(jsonString)
        assertThat(result).isEqualTo(originalAgeGroup)
    }

    @Test
    fun `AgeGroup is deserialized correctly when null`() {
        cache.ageGroup = null
        val result = cache.ageGroup
        assertThat(result).isNull()
    }
}

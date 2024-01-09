package com.simprints.feature.orchestrator.cache

import android.content.SharedPreferences
import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.infra.security.SecurityManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

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
    fun `Clears steps when requested`() {
        val result = cache.clearSteps()

        verify { prefs.edit().remove(any()) }
    }
}

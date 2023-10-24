package com.simprints.feature.orchestrator.cache

import android.content.SharedPreferences
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
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
    private lateinit var encodingUtils: EncodingUtils

    @MockK
    private lateinit var jsonHelper: JsonHelper

    @MockK
    private lateinit var parcelableConverter: ParcelableConverter

    private lateinit var cache: OrchestratorCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns prefs

        cache = OrchestratorCache(
            securityManager,
            encodingUtils,
            jsonHelper,
            parcelableConverter
        )
    }

    @Test
    fun `Resets request if passed null`() {
        cache.actionRequest = null

        verify { prefs.edit().remove(any()) }
    }

    @Test
    fun `Stores request if passed value`() {
        every { parcelableConverter.marshall(any()) } returns ByteArray(0)
        every { encodingUtils.byteArrayToBase64(any()) } returns "result"

        cache.actionRequest = mockk()

        verify {
            parcelableConverter.marshall(any())
            encodingUtils.byteArrayToBase64(any())
            prefs.edit().putString(any(), eq("result"))
        }
    }

    @Test
    fun `Restores request if requested value`() {
        every { prefs.getString(any(), any()) } returns "result"
        every { encodingUtils.base64ToBytes(any()) } returns ByteArray(0)
        every { parcelableConverter.unmarshall<OrchestratorCache.ActionRequestWrapper>(any(), any()) } returns mockk {
            every { request } returns mockk()
        }

        val result = cache.actionRequest

        verify {
            encodingUtils.base64ToBytes(any())
            parcelableConverter.unmarshall<OrchestratorCache.ActionRequestWrapper>(any(), any())
            prefs.getString(any(), any())
        }
    }

    @Test
    fun `Skips marshalling if no stored request`() {
        every { prefs.getString(any(), any()) } returns null

        val result = cache.actionRequest

        verify(exactly = 0) {
            encodingUtils.base64ToBytes(any())
            parcelableConverter.unmarshall<OrchestratorCache.ActionRequestWrapper>(any(), any())
        }
    }

    @Test
    fun `Stores steps if passed value`() {
        every { parcelableConverter.marshall(any()) } returns ByteArray(0)
        every { jsonHelper.toJson(any()) } returns "[]"

        cache.steps = listOf(mockk(), mockk())

        verify(exactly = 2) {
            parcelableConverter.marshall(any())
        }
        verify {
            jsonHelper.toJson(any())
            prefs.edit().putString(any(), eq("[]"))
        }
    }

    @Test
    fun `Restores steps if requested value`() {
        every { prefs.getString(any(), any()) } returns "[]"
        every { jsonHelper.fromJson<List<String>>(any()) } returns listOf("", "")
        every { parcelableConverter.unmarshall<OrchestratorCache.StepWrapper>(any(), any()) } returns mockk {
            every { step } returns mockk()
        }

        val result = cache.steps

        verify(exactly = 2) {
            parcelableConverter.unmarshall<OrchestratorCache.StepWrapper>(any(), any())
        }
        verify {
            jsonHelper.fromJson<List<String>>(any())
            prefs.getString(any(), any())
        }
    }


    @Test
    fun `Skips unmarshalling if no steps`() {
        every { prefs.getString(any(), any()) } returns null

        val result = cache.steps

        verify(exactly = 0) {
            parcelableConverter.unmarshall<OrchestratorCache.StepWrapper>(any(), any())
            jsonHelper.fromJson<List<String>>(any())
        }
    }

    @Test
    fun `Clears steps when requested`() {
        val result = cache.clearSteps()

        verify { prefs.edit().remove(any()) }
    }
}

package com.simprints.infra.network.url

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.*
import com.simprints.infra.network.url.BaseUrlProviderImpl.Companion.DEFAULT_BASE_URL
import com.simprints.infra.security.SecurityManager
import io.mockk.*
import io.mockk.impl.annotations.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class BaseUrlProviderImplTest {
    companion object {
        private const val URL_SUFFIX = "/androidapi/v2/"

        private const val URL = "https://test"
        private const val URL_WITH_SUFFIX = "https://test$URL_SUFFIX"
    }

    @RelaxedMockK
    lateinit var ctx: Context

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var legacySharedPreferences: SharedPreferences

    @MockK
    private lateinit var legacyEditor: SharedPreferences.Editor

    @MockK
    private lateinit var secureSharedPreferences: SharedPreferences

    @MockK
    private lateinit var secureEditor: SharedPreferences.Editor

    private lateinit var baseUrlProviderImpl: BaseUrlProviderImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { ctx.getSharedPreferences(any(), any()) } returns legacySharedPreferences
        every { legacySharedPreferences.edit() } returns legacyEditor

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns secureSharedPreferences
        every { secureSharedPreferences.edit() } returns secureEditor
        every { secureEditor.putString(any(), any()) } returns secureEditor

        baseUrlProviderImpl = BaseUrlProviderImpl(ctx, securityManager)
    }

    @Test
    fun `should migrate data from legacy prefs to secure prefs`() {
        every { legacySharedPreferences.contains(any()) } returns true
        every { legacySharedPreferences.getString(any(), any()) } returns "old-value"

        val result = baseUrlProviderImpl.getApiBaseUrl()

        verify { secureEditor.putString(any(), any()) }
        verify(exactly = 1) {
            legacyEditor.clear()
            legacyEditor.commit()
            secureEditor.commit()
        }
    }

    @Test
    fun `get api base url should return the actual url`() {
        every { secureSharedPreferences.getString(any(), any()) } returns URL

        val url = baseUrlProviderImpl.getApiBaseUrl()

        assertThat(url).isEqualTo(URL)
    }

    @Test
    fun `get api base url prefix should return the actual url`() {
        every { secureSharedPreferences.getString(any(), any()) } returns URL_WITH_SUFFIX

        val url = baseUrlProviderImpl.getApiBaseUrlPrefix()

        assertThat(url).isEqualTo(URL)
    }

    @Test
    fun `set api base url should set the url to the default one when the one passed is null`() {
        baseUrlProviderImpl.setApiBaseUrl(null)

        verify(exactly = 1) { secureEditor.putString(any(), DEFAULT_BASE_URL) }
    }

    @Test
    fun `set api base url should set the url and adds the the base url`() {
        val url = "https://url.com"
        baseUrlProviderImpl.setApiBaseUrl(url)

        verify(exactly = 1) { secureEditor.putString(any(), "$url$URL_SUFFIX") }
    }

    @Test
    fun `set api base url should set the url and adds the https prefix if missing`() {
        val url = "url.com"
        baseUrlProviderImpl.setApiBaseUrl(url)

        verify(exactly = 1) { secureEditor.putString(any(), "https://$url$URL_SUFFIX") }
    }

    @Test
    fun `reset api base url should set the url to the default one`() {
        baseUrlProviderImpl.resetApiBaseUrl()

        verify(exactly = 1) { secureEditor.putString(any(), DEFAULT_BASE_URL) }
    }
}

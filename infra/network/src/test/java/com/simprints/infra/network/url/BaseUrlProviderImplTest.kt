package com.simprints.infra.network.url

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.network.url.BaseUrlProviderImpl.Companion.DEFAULT_BASE_URL
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
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

    @RelaxedMockK
    lateinit var sharedPreferences: SharedPreferences

    @RelaxedMockK
    lateinit var editor: SharedPreferences.Editor

    private lateinit var baseUrlProviderImpl: BaseUrlProviderImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { ctx.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor

        baseUrlProviderImpl = BaseUrlProviderImpl(ctx)
    }

    @Test
    fun `get api base url should return the actual url`() {
        every { sharedPreferences.getString(any(), any()) } returns URL

        val url = baseUrlProviderImpl.getApiBaseUrl()

        assertThat(url).isEqualTo(URL)
    }

    @Test
    fun `get api base url prefix should return the actual url`() {
        every { sharedPreferences.getString(any(), any()) } returns URL_WITH_SUFFIX

        val url = baseUrlProviderImpl.getApiBaseUrlPrefix()

        assertThat(url).isEqualTo(URL)
    }

    @Test
    fun `set api base url should set the url to the default one when the one passed is null`() {
        baseUrlProviderImpl.setApiBaseUrl(null)

        verify(exactly = 1) { editor.putString(any(), DEFAULT_BASE_URL) }
    }

    @Test
    fun `set api base url should set the url and adds the the base url`() {
        val url = "https://url.com"
        baseUrlProviderImpl.setApiBaseUrl(url)

        verify(exactly = 1) { editor.putString(any(), "$url$URL_SUFFIX") }
    }

    @Test
    fun `set api base url should set the url and adds the https prefix if missing`() {
        val url = "url.com"
        baseUrlProviderImpl.setApiBaseUrl(url)

        verify(exactly = 1) { editor.putString(any(), "https://$url$URL_SUFFIX") }
    }

    @Test
    fun `reset api base url should set the url to the default one`() {
        baseUrlProviderImpl.resetApiBaseUrl()

        verify(exactly = 1) { editor.putString(any(), DEFAULT_BASE_URL) }
    }
}

package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.NetworkConstants.Companion.BASE_URL_SUFFIX
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class BaseUrlProviderImplTest {

    @MockK lateinit var mockSettingsPreferencesManager: SettingsPreferencesManager

    private lateinit var baseUrlProvider: BaseUrlProviderImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockSettingsPreferencesManager.apiBaseUrl } returns MOCK_URL
        baseUrlProvider = BaseUrlProviderImpl(mockSettingsPreferencesManager)
    }

    @Test
    fun shouldReturnBaseUrlFromSharedPreferences() {
        assertThat(baseUrlProvider.getApiBaseUrl()).isEqualTo(MOCK_URL)
    }

    @Test
    fun shouldSaveNewBaseUrlToSharedPreferences() {
        baseUrlProvider.setApiBaseUrl("https://new-url")

        verify { mockSettingsPreferencesManager.apiBaseUrl = any() }
    }

    @Test
    fun whenSettingNewBaseUrl_shouldAppendSuffix() {
        baseUrlProvider.setApiBaseUrl("https://new-url")

        verify { mockSettingsPreferencesManager.apiBaseUrl = "https://new-url$BASE_URL_SUFFIX" }
    }

    @Test
    fun whenSettingNullBaseUrl_shouldSaveDefaultToSharedPreferences() {
        baseUrlProvider.setApiBaseUrl(null)

        verify { mockSettingsPreferencesManager.apiBaseUrl = DEFAULT_BASE_URL }
    }

    @Test
    fun whenSettingNewBaseUrlWithoutHttpsPrefix_shouldAppendPrefix() {
        baseUrlProvider.setApiBaseUrl("new-url")

        verify { mockSettingsPreferencesManager.apiBaseUrl = "https://new-url$BASE_URL_SUFFIX" }
    }

    private companion object {
        const val MOCK_URL = "https://mock-url"
    }

}

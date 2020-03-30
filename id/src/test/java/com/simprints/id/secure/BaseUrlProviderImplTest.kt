package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.NetworkConstants.Companion.BASE_URL_SUFFIX
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl.Companion.IMAGE_STORAGE_BUCKET_URL_DEFAULT
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class BaseUrlProviderImplTest {

    @MockK lateinit var mockSettingsPreferencesManager: SettingsPreferencesManager
    @MockK lateinit var mockRemoteProjectInfoProvider: RemoteProjectInfoProvider

    private lateinit var baseUrlProvider: BaseUrlProviderImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockSettingsPreferencesManager.apiBaseUrl } returns MOCK_URL
        every { mockRemoteProjectInfoProvider.getProjectName() } returns MOCK_PROJECT_NAME

        baseUrlProvider = BaseUrlProviderImpl(
            mockSettingsPreferencesManager,
            mockRemoteProjectInfoProvider
        )
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

    @Test
    fun whenResettingBaseUrl_shouldSaveDefaultToSharedPreferences() {
        baseUrlProvider.resetApiBaseUrl()

        verify { mockSettingsPreferencesManager.apiBaseUrl = DEFAULT_BASE_URL }
    }

    @Test
    fun shouldReturnImageStorageBucketUrlFromSharedPreferences() {
        val expected = "mock-bucket-url"
        every { mockSettingsPreferencesManager.imageStorageBucketUrl } returns expected

        assertThat(baseUrlProvider.getImageStorageBucketUrl()).isEqualTo(expected)
    }

    @Test
    fun whenNoValueIsFoundInSharedPreferences_shouldUseDefaultImageStorageBucketUrl() {
        every { mockSettingsPreferencesManager.imageStorageBucketUrl } returns ""
        val expected = "gs://$MOCK_PROJECT_NAME-images-eu"

        assertThat(baseUrlProvider.getImageStorageBucketUrl()).isEqualTo(expected)
    }

    @Test
    fun whenImageStorageBucketUrlIsStoredInSharedPreferences_shouldNotUseDefault() {
        val expected = "mock-bucket-url"
        every { mockSettingsPreferencesManager.imageStorageBucketUrl } returns expected

        baseUrlProvider.getImageStorageBucketUrl()

        verify(exactly = 0) { mockRemoteProjectInfoProvider.getProjectName() }
    }

    @Test
    fun shouldSaveNewImageStorageBucketUrlToSharedPreferences() {
        baseUrlProvider.setImageStorageBucketUrl("mock-bucket-url")

        verify { mockSettingsPreferencesManager.imageStorageBucketUrl = any() }
    }

    @Test
    fun whenResettingImageStorageBucketUrl_shouldSaveDefaultToSharedPreferences() {
        baseUrlProvider.resetImageStorageBucketUrl()

        verify {
            mockSettingsPreferencesManager.imageStorageBucketUrl = IMAGE_STORAGE_BUCKET_URL_DEFAULT
        }
    }

    private companion object {
        const val MOCK_URL = "https://mock-url"
        const val MOCK_PROJECT_NAME = "mock-project"
    }

}

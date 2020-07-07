package com.simprints.id.network

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.network.NetworkConstants.Companion.BASE_URL_SUFFIX
import com.simprints.id.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class BaseUrlProviderImplTest {

    @MockK lateinit var mockSettingsPreferencesManager: SettingsPreferencesManager
    @MockK lateinit var mockProjectLocalDataSource: ProjectLocalDataSource
    @MockK lateinit var mockLoginInfoManager: LoginInfoManager

    private lateinit var baseUrlProvider: BaseUrlProviderImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockSettingsPreferencesManager.apiBaseUrl } returns MOCK_URL

        baseUrlProvider = BaseUrlProviderImpl(
                mockSettingsPreferencesManager,
                mockProjectLocalDataSource,
                mockLoginInfoManager
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
    @ExperimentalCoroutinesApi
    fun shouldReturnImageStorageBucketUrlFromProjectLocalDataSource() = runBlockingTest {
        val expected = "mock-bucket-url"
        every { mockLoginInfoManager.getSignedInProjectIdOrEmpty() } returns "mock-project-id"
        coEvery {
            mockProjectLocalDataSource.load(any())
        } returns Project(
            "id",
            "name",
            "description",
            "creator",
            expected
        )

        assertThat(baseUrlProvider.getImageStorageBucketUrl()).isEqualTo(expected)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun whenNoValueIsFoundInLoginInfoManager_shouldReturnNull() = runBlockingTest {
        every { mockLoginInfoManager.getSignedInProjectIdOrEmpty() } returns ""

        assertThat(baseUrlProvider.getImageStorageBucketUrl()).isNull()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun whenNoValueIsFoundInProjectLocalDataSource_shouldReturnDefaultImageStorageBucketUrl() {
        every { mockLoginInfoManager.getSignedInProjectIdOrEmpty() } returns "mock-project-id"
        coEvery { mockProjectLocalDataSource.load(any()) } returns null

        val expected = "gs://mock-project-id-images-eu"
        runBlockingTest {
            assertThat(baseUrlProvider.getImageStorageBucketUrl()).isEqualTo(expected)
        }
    }

    private companion object {
        const val MOCK_URL = "https://mock-url"
    }

}

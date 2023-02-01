package com.simprints.id.activities.longConsent


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.PrivacyNoticeResult
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class PrivacyNoticeViewModelTest {
    companion object {
        private const val PROJECT_ID = "projectId"
        private const val LANGUAGE = "en"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val configManager = mockk<ConfigManager> {
        coEvery { getDeviceConfiguration() } returns DeviceConfiguration(
            LANGUAGE,
            listOf(),
            listOf(),
            ""
        )
    }
    private val loginManager = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns PROJECT_ID
    }
    private val privacyNoticeViewModel = PrivacyNoticeViewModel(
        configManager,
        loginManager,
        testCoroutineRule.testCoroutineDispatcher,
    )

    @Test
    fun retrievePrivacyNotice_shouldReturn_ContentAvailable_wheneverSucceedValue_isReturned() {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(LANGUAGE),
            PrivacyNoticeResult.Succeed(LANGUAGE, "some long consent")
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.getPrivacyNoticeViewStateLiveData()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        assertThat(value).isInstanceOf(PrivacyNoticeState.ConsentAvailable::class.java)
    }

    @Test
    fun retrievePrivacyNotice_shouldReturn_ContentNotAvailable_wheneverFailedBecauseBackendMaintenance_isReturned() {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(LANGUAGE),
            PrivacyNoticeResult.FailedBecauseBackendMaintenance(LANGUAGE, Throwable())
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.getPrivacyNoticeViewStateLiveData()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        assertThat(value).isInstanceOf(PrivacyNoticeState.ConsentNotAvailableBecauseBackendMaintenance::class.java)
    }
}

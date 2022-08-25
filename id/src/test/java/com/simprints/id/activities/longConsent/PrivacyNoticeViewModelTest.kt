package com.simprints.id.activities.longConsent


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
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
        private const val LANGUAGE = "en"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val longConsentRepository = mockk<LongConsentRepository>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getDeviceConfiguration() } returns DeviceConfiguration(
            LANGUAGE,
            listOf(),
            listOf()
        )
    }
    private val privacyNoticeViewModel = PrivacyNoticeViewModel(
        longConsentRepository,
        configManager,
        testCoroutineRule.testCoroutineDispatcher,
    )

    @Test
    fun retrievePrivacyNotice_shouldReturn_ContentAvailable_wheneverSucceedValue_isReturned() {
        every { longConsentRepository.getLongConsentResultForLanguage(LANGUAGE) } returns flowOf(
            LongConsentFetchResult.InProgress(LANGUAGE),
            LongConsentFetchResult.Succeed(LANGUAGE, "some long consent")
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.getPrivacyNoticeViewStateLiveData()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        assertThat(value).isInstanceOf(PrivacyNoticeViewState.ConsentAvailable::class.java)
    }

    @Test
    fun retrievePrivacyNotice_shouldReturn_ContentNotAvailable_wheneverFailedBecauseBackendMaintenance_isReturned() {
        every { longConsentRepository.getLongConsentResultForLanguage(LANGUAGE) } returns flowOf(
            LongConsentFetchResult.InProgress(LANGUAGE),
            LongConsentFetchResult.FailedBecauseBackendMaintenance(LANGUAGE, Throwable())
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.getPrivacyNoticeViewStateLiveData()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        assertThat(value).isInstanceOf(PrivacyNoticeViewState.ConsentNotAvailableBecauseBackendMaintenance::class.java)
    }
}

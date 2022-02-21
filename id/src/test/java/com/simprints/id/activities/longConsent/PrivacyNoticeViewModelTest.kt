package com.simprints.id.activities.longConsent


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PrivacyNoticeViewModelTest {
    private val language = "en"

    @MockK
    lateinit var longConsentRepository: LongConsentRepository
    lateinit var privacyNoticeViewModel: PrivacyNoticeViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val dispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        privacyNoticeViewModel = PrivacyNoticeViewModel(
            longConsentRepository,
            language,
            dispatcherProvider
        )
    }

    @Test
    fun retrievePrivacyNotice_shouldReturn_ContentAvailable_wheneverSucceedValue_isReturned() {
        every { longConsentRepository.getLongConsentResultForLanguage(language) } returns flowOf(
            LongConsentFetchResult.InProgress(language),
            LongConsentFetchResult.Succeed(language, "some long consent")
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.getPrivacyNoticeViewStateLiveData()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        assertThat(value).isInstanceOf(PrivacyNoticeViewState.ConsentAvailable::class.java)
    }

    @Test
    fun retrievePrivacyNotice_shouldReturn_ContentNotAvailable_wheneverFailedBecauseBackendMaintenance_isReturned() {
        every { longConsentRepository.getLongConsentResultForLanguage(language) } returns flowOf(
            LongConsentFetchResult.InProgress(language),
            LongConsentFetchResult.FailedBecauseBackendMaintenance(language, Throwable())
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.getPrivacyNoticeViewStateLiveData()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        assertThat(value).isInstanceOf(PrivacyNoticeViewState.ConsentNotAvailableBecauseBackendMaintenance::class.java)
    }
}

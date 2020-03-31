package com.simprints.id.activities.longConsent

import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class PrivacyNoticeViewModelTest {

    companion object {
        private const val DEFAULT_LANGUAGE = "en"
        private const val LONG_CONSENT_TEXT = "Long consent text"
    }

    @MockK lateinit var longConsentRepository: LongConsentRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun setLanguage_consentPresentInLocal_shouldUpdateLiveData() = runBlockingTest {
        every { longConsentRepository.longConsentTextLiveData } returns MutableLiveData(LONG_CONSENT_TEXT)

        val viewModel = PrivacyNoticeViewModel(longConsentRepository, DEFAULT_LANGUAGE)
        viewModel.start()
        val actualText = viewModel.longConsentTextLiveData.value


        assertThat(actualText).isEqualTo(LONG_CONSENT_TEXT)
    }

    @Test
    fun downloadLongConsentSucceeds_shouldUpdateLiveDataValues() {
        every { longConsentRepository.isDownloadSuccessfulLiveData } returns MutableLiveData(true)
        every { longConsentRepository.longConsentTextLiveData } returns MutableLiveData(LONG_CONSENT_TEXT)

        val viewModel = PrivacyNoticeViewModel(longConsentRepository, DEFAULT_LANGUAGE)
        viewModel.start()
        viewModel.downloadLongConsent()
        val isDownloadSuccessful = viewModel.isDownloadSuccessfulLiveData.value
        val actualText = viewModel.longConsentTextLiveData.value

        assertThat(isDownloadSuccessful).isTrue()
        assertThat(actualText).isEqualTo(LONG_CONSENT_TEXT)
    }

    @Test
    fun downloadLongConsentFails_shouldUpdateLiveDataValues() {
        every { longConsentRepository.isDownloadSuccessfulLiveData } returns MutableLiveData(false)
        every { longConsentRepository.longConsentTextLiveData } returns MutableLiveData("")

        val viewModel = PrivacyNoticeViewModel(longConsentRepository, DEFAULT_LANGUAGE)
        viewModel.start()
        viewModel.downloadLongConsent()
        val isDownloadSuccessful = viewModel.isDownloadSuccessfulLiveData.value
        val actualText = viewModel.longConsentTextLiveData.value

        assertThat(isDownloadSuccessful).isFalse()
        assertThat(actualText).isEmpty()
    }

}

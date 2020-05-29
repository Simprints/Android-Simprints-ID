package com.simprints.id.data.consent.longconsent

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.longConsent.PrivacyNoticeViewState.ConsentAvailable
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LongConsentRepositoryImplTest {

    companion object {
        const val DEFAULT_LANGUAGE = "en"
        private const val PROJECT_ID_TEST = "project_id_test"
        private const val LONG_CONSENT_TEXT = "long consent text"
    }

    @MockK lateinit var longConsentLocalDataSourceMock: LongConsentLocalDataSource
    @MockK lateinit var longConsentRemoteDataSourceMock: LongConsentRemoteDataSource

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun download_consentInLocal_shouldReturnTheLocalCopy() {
        runBlocking {
            every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns LONG_CONSENT_TEXT
            val longConsentRepository = LongConsentRepositoryImpl(longConsentLocalDataSourceMock, longConsentRemoteDataSourceMock, mockk())

            val states = longConsentRepository.downloadLongConsent(arrayOf(DEFAULT_LANGUAGE))

            assertThat(states.poll()).isEqualTo(mapOf(DEFAULT_LANGUAGE to ConsentAvailable(DEFAULT_LANGUAGE, LONG_CONSENT_TEXT)))
            verify(exactly = 1) { longConsentLocalDataSourceMock.getLongConsentText(DEFAULT_LANGUAGE) }
        }
    }

    @Test
    fun deleteLongConsents_shouldDeleteLongConsentsFromRepository() {
        val longConsentRepository = LongConsentRepositoryImpl(longConsentLocalDataSourceMock, longConsentRemoteDataSourceMock, mockk())

        longConsentRepository.deleteLongConsents()

        verify(exactly = 1) { longConsentLocalDataSourceMock.deleteLongConsents() }
    }
}

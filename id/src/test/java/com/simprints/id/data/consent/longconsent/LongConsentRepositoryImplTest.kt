package com.simprints.id.data.consent.longconsent

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.activities.longConsent.PrivacyNoticeViewState.ConsentAvailable
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.toCollection
import org.junit.Rule
import org.junit.Test

class LongConsentRepositoryImplTest {

    companion object {
        const val DEFAULT_LANGUAGE = "en"
        private const val PROJECT_ID_TEST = "project_id_test"
        private const val LONG_CONSENT_TEXT = "long consent text"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = object : DispatcherProvider {
        override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
    }

    private val longConsentLocalDataSourceMock: LongConsentLocalDataSource = mockk(relaxed = true)
    private val longConsentRemoteDataSourceMock: LongConsentRemoteDataSource = mockk(relaxed = true)

    private val longConsentRepository = LongConsentRepositoryImpl(
        longConsentLocalDataSourceMock,
        longConsentRemoteDataSourceMock,
        mockk(),
        testDispatcherProvider
    )

    @Test
    fun download_consentInLocal_shouldReturnTheLocalCopy() = testCoroutineRule.runBlockingTest {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns LONG_CONSENT_TEXT

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(1)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.Succeed(DEFAULT_LANGUAGE, LONG_CONSENT_TEXT))
        }
        verify(exactly = 1) { longConsentLocalDataSourceMock.getLongConsentText(DEFAULT_LANGUAGE) }
    }

    @Test
    fun deleteLongConsents_shouldDeleteLongConsentsFromRepository() {
        longConsentRepository.deleteLongConsents()

        verify(exactly = 1) { longConsentLocalDataSourceMock.deleteLongConsents() }
    }
}

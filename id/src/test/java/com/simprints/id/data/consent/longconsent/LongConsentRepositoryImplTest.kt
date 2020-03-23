package com.simprints.id.data.consent.longconsent

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
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
    @MockK lateinit var loginInfoManagerMock: LoginInfoManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns PROJECT_ID_TEST
    }

    @Test
    fun setLanguage_shouldSetLanguageAndUpdateTextIfPresentInLocal() {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns LONG_CONSENT_TEXT
        val longConsentRepository = LongConsentRepositoryImpl(longConsentLocalDataSourceMock, loginInfoManagerMock, mockk())

        longConsentRepository.setLanguage(DEFAULT_LANGUAGE)

        assertThat(longConsentRepository.language).isEqualTo(DEFAULT_LANGUAGE)
        assertThat(longConsentRepository.longConsentText.value).isEqualTo(LONG_CONSENT_TEXT)
    }

    @Test
    fun setLanguageAndLongConsentNotPresentInLocal_shouldSetEmptyString() {
        val longConsentRepository = LongConsentRepositoryImpl(longConsentLocalDataSourceMock, loginInfoManagerMock, mockk())

        longConsentRepository.setLanguage(DEFAULT_LANGUAGE)

        assertThat(longConsentRepository.language).isEqualTo(DEFAULT_LANGUAGE)
        assertThat(longConsentRepository.longConsentText.value).isEmpty()
    }

    @Test
    fun deleteLongConsents_shouldDeleteLongConsentsFromRepository() {
        val longConsentRepository = LongConsentRepositoryImpl(longConsentLocalDataSourceMock, loginInfoManagerMock, mockk())

        longConsentRepository.deleteLongConsents()

        verify(exactly = 1) { longConsentLocalDataSourceMock.deleteLongConsents() }
    }
}

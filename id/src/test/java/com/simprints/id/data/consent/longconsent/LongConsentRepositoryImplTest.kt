package com.simprints.id.data.consent.longconsent

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toCollection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class LongConsentRepositoryImplTest {

    companion object {
        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_LONG_CONSENT_TEXT = "Very long consent indeed"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val longConsentLocalDataSourceMock: LongConsentLocalDataSource = mockk(relaxed = true)
    private val longConsentRemoteDataSourceMock: LongConsentRemoteDataSource = mockk(relaxed = true)

    private val longConsentRepository = LongConsentRepositoryImpl(
        longConsentLocalDataSourceMock,
        longConsentRemoteDataSourceMock
    )

    @Test
    fun `Delete all consents`() {
        longConsentRepository.deleteLongConsents()

        verify(exactly = 1) { longConsentLocalDataSourceMock.deleteLongConsents() }
    }

    @Test
    fun `Return local consent`() = testCoroutineRule.runBlockingTest {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns DEFAULT_LONG_CONSENT_TEXT

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(1)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.Succeed(DEFAULT_LANGUAGE, DEFAULT_LONG_CONSENT_TEXT))
        }
    }

    @Test
    fun `Download the consent in multiple parts`() = testCoroutineRule.runBlockingTest {
        val bytesSize = 2048
        val consentBytes = Random.nextBytes(bytesSize)
        val consentText = consentBytes.toString(Charset.defaultCharset())

        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns ""
        every { longConsentLocalDataSourceMock.createFileForLanguage(any()) } returns File.createTempFile("test", null)
        coEvery { longConsentRemoteDataSourceMock.downloadLongConsent(any()) } returns LongConsentRemoteDataSource.File(
            consentBytes
        )

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.InProgress(DEFAULT_LANGUAGE))
            assertThat(get(1)).isEqualTo(LongConsentFetchResult.Succeed(DEFAULT_LANGUAGE, consentText))
        }
    }

    @Test
    fun `Return error on something wrong`() = testCoroutineRule.runBlockingTest {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns ""
        coEvery { longConsentRemoteDataSourceMock.downloadLongConsent(any()) } throws IOException()

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.InProgress(DEFAULT_LANGUAGE))
            assertThat(get(1)).isInstanceOf(LongConsentFetchResult.Failed::class.java)
        }
    }
}

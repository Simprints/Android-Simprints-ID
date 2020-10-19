package com.simprints.id.data.consent.longconsent

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toCollection
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import kotlin.random.Random

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
        longConsentRemoteDataSourceMock,
        mockk()
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
        val byteArrayInputStream = ByteArrayInputStream(consentBytes)

        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns ""
        every { longConsentLocalDataSourceMock.createFileForLanguage(any()) } returns File.createTempFile("test", null)
        coEvery { longConsentRemoteDataSourceMock.downloadLongConsent(any()) } returns LongConsentRemoteDataSource.Stream(
            byteArrayInputStream,
            bytesSize.toLong()
        )

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.Progress(DEFAULT_LANGUAGE, 0.5f))
            assertThat(get(1)).isEqualTo(LongConsentFetchResult.Progress(DEFAULT_LANGUAGE, 1f))
            assertThat(get(2)).isEqualTo(LongConsentFetchResult.Succeed(DEFAULT_LANGUAGE, consentText))
        }
    }

    @Test
    fun `Return error on something wrong`() = testCoroutineRule.runBlockingTest {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns ""
        coEvery { longConsentRemoteDataSourceMock.downloadLongConsent(any()) } throws IOException()

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(1)
            assertThat(get(0)).isInstanceOf(LongConsentFetchResult.Failed::class.java)
        }
    }
}

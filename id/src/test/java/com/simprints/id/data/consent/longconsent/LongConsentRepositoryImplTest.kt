package com.simprints.id.data.consent.longconsent

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.nio.charset.Charset
import kotlin.random.Random

class LongConsentRepositoryImplTest {

    companion object {
        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_LONG_CONSENT_TEXT = "Very long consent indeed"
    }

    private val longConsentLocalDataSourceMock: LongConsentLocalDataSource = mockk(relaxed = true)
    private val longConsentRemoteDataSourceMock: LongConsentRemoteDataSource = mockk(relaxed = true)

    private val longConsentRepository = LongConsentRepositoryImpl(
        longConsentLocalDataSourceMock,
        longConsentRemoteDataSourceMock
    )

    @Test
    fun `delete all consents`() {
        longConsentRepository.deleteLongConsents()

        verify(exactly = 1) { longConsentLocalDataSourceMock.deleteLongConsents() }
    }

    @Test
    fun `return local consent`() = runTest(StandardTestDispatcher()) {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns DEFAULT_LONG_CONSENT_TEXT

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(1)
            assertThat(get(0)).isEqualTo(
                LongConsentFetchResult.Succeed(
                    DEFAULT_LANGUAGE,
                    DEFAULT_LONG_CONSENT_TEXT
                )
            )
        }
    }

    @Test
    fun `return error on local consent reading exception`() = runTest(StandardTestDispatcher()) {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } throws IOException()

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(1)
            assertThat(get(0)).isInstanceOf(LongConsentFetchResult.Failed::class.java)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Test
    fun `download the consent in multiple parts`() = runTest(StandardTestDispatcher()) {
        val bytesSize = 2048
        val consentBytes = Random.nextBytes(bytesSize)
        val consentText = consentBytes.toString(Charset.defaultCharset())

        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns ""
        every { longConsentLocalDataSourceMock.createFileForLanguage(any()) } returns File.createTempFile(
            "test",
            null
        )
        coEvery { longConsentRemoteDataSourceMock.downloadLongConsent(any()) } returns LongConsentRemoteDataSource.File(
            consentBytes
        )

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.InProgress(DEFAULT_LANGUAGE))
            assertThat(get(1)).isEqualTo(
                LongConsentFetchResult.Succeed(
                    DEFAULT_LANGUAGE,
                    consentText
                )
            )
        }
    }

    @Test
    fun `return error on something wrong`() = runTest(StandardTestDispatcher()) {
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

    @Test
    fun `return error on failed network connection`() = runTest(StandardTestDispatcher()) {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns ""
        coEvery { longConsentRemoteDataSourceMock.downloadLongConsent(any()) } throws Exception(
            SocketTimeoutException()
        )

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.InProgress(DEFAULT_LANGUAGE))
            assertThat(get(1)).isInstanceOf(LongConsentFetchResult.Failed::class.java)
        }
    }

    @Test
    fun `return error on generic exception`() = runTest(StandardTestDispatcher()) {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns ""
        coEvery { longConsentRemoteDataSourceMock.downloadLongConsent(any()) } throws Exception()

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.InProgress(DEFAULT_LANGUAGE))
            assertThat(get(1)).isInstanceOf(LongConsentFetchResult.Failed::class.java)
        }
    }

    @Test
    fun `return backend error on backend maintenance`() = runTest(StandardTestDispatcher()) {
        every { longConsentLocalDataSourceMock.getLongConsentText(any()) } returns ""
        coEvery { longConsentRemoteDataSourceMock.downloadLongConsent(any()) } throws BackendMaintenanceException(
            estimatedOutage = 100
        )

        val states = mutableListOf<LongConsentFetchResult>()
        longConsentRepository.getLongConsentResultForLanguage(DEFAULT_LANGUAGE).toCollection(states)

        with(states) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LongConsentFetchResult.InProgress(DEFAULT_LANGUAGE))
            assertThat(get(1)).isInstanceOf(LongConsentFetchResult.FailedBecauseBackendMaintenance::class.java)
        }
    }
}

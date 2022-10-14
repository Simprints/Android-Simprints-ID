package com.simprints.id.data.consent.longconsent.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.file.FileUrl
import com.simprints.id.data.file.FileUrlRemoteInterface
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LongConsentRemoteDataSourceImplTest {

    @MockK
    lateinit var loginManagerMock: LoginManager

    @MockK
    lateinit var remoteApi: FileUrlRemoteInterface

    @MockK
    lateinit var consentDownloader: (FileUrl) -> ByteArray

    @MockK
    lateinit var remoteApiClient: SimNetwork.SimApiClient<FileUrlRemoteInterface>


    private lateinit var remoteDataSource: LongConsentRemoteDataSourceImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { remoteApiClient.api } returns remoteApi
        coEvery { loginManagerMock.buildClient(FileUrlRemoteInterface::class) } returns remoteApiClient
        every { loginManagerMock.getSignedInProjectIdOrEmpty() } returns PROJECT_ID_TEST

        remoteDataSource = LongConsentRemoteDataSourceImpl(
            loginManagerMock,
            consentDownloader
        )
    }

    @Test
    fun `should return file containing contents of long-consent string`() =
        runTest(StandardTestDispatcher()) {
            val longConsentByteStream = "some really long consent privacy notice.".toByteArray()
            val fileUrl = FileUrl("https://simprints/mock-longconsent-file-url")

            every { consentDownloader.invoke(any()) } returns longConsentByteStream
            coEvery { remoteApi.getFileUrl(any(), any()) } returns fileUrl

            val longConsent = remoteDataSource.downloadLongConsent(DEFAULT_LANGUAGE)

            assertThat(longConsent.bytes).isEqualTo(longConsentByteStream)
        }

    companion object {
        private const val DEFAULT_LANGUAGE = "EN"
        private const val PROJECT_ID_TEST = "project_id_test"
    }

}

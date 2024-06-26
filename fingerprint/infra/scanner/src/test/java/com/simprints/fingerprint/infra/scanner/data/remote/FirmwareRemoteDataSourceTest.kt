package com.simprints.fingerprint.infra.scanner.data.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.data.remote.network.FingerprintFileDownloader
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirmwareRemoteDataSourceTest {
    private val fingerprintFileDownloaderMock: FingerprintFileDownloader = mockk()

    private val firmwareRemoteDataSource =
        FirmwareRemoteDataSource(fingerprintFileDownloaderMock)

    @Before
    fun setup() {
        coEvery { fingerprintFileDownloaderMock.getFileUrl(any()) } returns SOME_URL
    }

    @Test
    fun downloadFile_correctlyForwardsDownload() = runTest {
        coEvery { fingerprintFileDownloaderMock.download(eq(SOME_URL)) } returns SOME_BIN

        val bytes = firmwareRemoteDataSource.downloadFirmware(
            DownloadableFirmwareVersion(
                DownloadableFirmwareVersion.Chip.STM,
                STM_VERSION_HIGH
            )
        )

        assertThat(bytes.toList()).containsExactlyElementsIn(SOME_BIN.toList()).inOrder()
    }

    companion object {
        private const val STM_VERSION_HIGH = "1.E-1.2"

        private const val SOME_URL = "some.url.com"
        private val SOME_BIN = byteArrayOf(0x00, 0x01, 0x02)
    }
}

package com.simprints.infra.sync.firmware

import androidx.work.ListenableWorker.Result.Retry
import androidx.work.ListenableWorker.Result.Success
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.data.FirmwareRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class FirmwareFileUpdateWorkerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var firmwareRepository: FirmwareRepository

    private lateinit var worker: FirmwareFileUpdateWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        worker = FirmwareFileUpdateWorker(
            mockk(relaxed = true),
            mockk(relaxed = true),
            firmwareRepository,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `Returns success if all works`() = runTest {
        coJustRun { firmwareRepository.updateStoredFirmwareFilesWithLatest() }
        coJustRun { firmwareRepository.cleanUpOldFirmwareFiles() }

        val result = worker.doWork()

        assertThat(result).isInstanceOf(Success::class.java)
    }

    @Test
    fun `Returns retry if update fails`() = runTest {
        coEvery { firmwareRepository.updateStoredFirmwareFilesWithLatest() } throws IOException()

        val result = worker.doWork()

        assertThat(result).isInstanceOf(Retry::class.java)
    }

    @Test
    fun `Returns retry if cleanup fails`() = runTest {
        coJustRun { firmwareRepository.updateStoredFirmwareFilesWithLatest() }
        coEvery { firmwareRepository.cleanUpOldFirmwareFiles() } throws RuntimeException()

        val result = worker.doWork()

        assertThat(result).isInstanceOf(Retry::class.java)
    }
}

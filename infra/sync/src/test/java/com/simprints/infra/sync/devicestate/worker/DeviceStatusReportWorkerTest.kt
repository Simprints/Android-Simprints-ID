package com.simprints.infra.sync.devicestate.worker

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.sync.devicestate.ReportDeviceDataStateUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeviceStatusReportWorkerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var reportDeviceDataState: ReportDeviceDataStateUseCase

    private lateinit var worker: DeviceStatusReportWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        worker = DeviceStatusReportWorker(
            context = mockk(relaxed = true),
            params = mockk(relaxed = true),
            reportDeviceDataState = reportDeviceDataState,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `succeeds when the state is reported`() = runTest {
        coEvery { reportDeviceDataState() } returns true

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `succeeds when there is no signed-in project to report for`() = runTest {
        coEvery { reportDeviceDataState() } returns false

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `fails without retry when reporting throws`() = runTest {
        coEvery { reportDeviceDataState() } throws IllegalStateException("backend down")

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.failure())
    }
}

package com.simprints.id.services.config

import androidx.work.ListenableWorker.Result
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RemoteConfigWorkerTest {

    private val workManager = mockk<WorkManager>()
    private lateinit var worker: RemoteConfigWorker

    @Before
    fun setup() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager

        worker = RemoteConfigWorker(mockk(relaxed = true), mockk(relaxed = true))
    }

    @Test
    fun `worker should cancel itself`() = runTest {
        val result = worker.doWork()

        assertThat(result).isEqualTo(Result.success())
        verify(exactly = 1) { workManager.cancelUniqueWork(RemoteConfigWorker.WORK_NAME) }
    }
}

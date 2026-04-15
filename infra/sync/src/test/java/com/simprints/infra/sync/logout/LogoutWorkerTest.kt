package com.simprints.infra.sync.logout

import android.os.PowerManager
import androidx.work.ListenableWorker
import androidx.work.workDataOf
import com.google.common.truth.Truth
import com.simprints.infra.sync.SyncConstants
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LogoutWorkerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var logoutUseCase: LogoutUseCase

    private lateinit var worker: LogoutWorker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    private fun createWorker(isProjectEnded: Boolean = false): LogoutWorker = LogoutWorker(
        context = mockk(relaxed = true) {
            every { getSystemService<PowerManager>(any()) } returns mockk {
                every { isIgnoringBatteryOptimizations(any()) } returns true
            }
        },
        params = mockk(relaxed = true) {
            every { inputData } returns workDataOf(SyncConstants.LOGOUT_INPUT_IS_PROJECT_ENDED to isProjectEnded)
        },
        logoutUseCase = logoutUseCase,
        dispatcher = testCoroutineRule.testCoroutineDispatcher,
    )

    @Test
    fun `doWork calls logoutUseCase with isProjectEnded=false by default`() = runTest {
        worker = createWorker(isProjectEnded = false)

        worker.doWork()

        coVerify { logoutUseCase(false) }
    }

    @Test
    fun `doWork calls logoutUseCase with isProjectEnded=true when set in input data`() = runTest {
        worker = createWorker(isProjectEnded = true)

        worker.doWork()

        coVerify { logoutUseCase(true) }
    }

    @Test
    fun `doWork returns success on normal execution`() = runTest {
        worker = createWorker()

        val result = worker.doWork()

        Truth.assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `doWork returns failure when logoutUseCase throws`() = runTest {
        worker = createWorker()
        coEvery { logoutUseCase(any()) } throws RuntimeException("logout failed")

        val result = worker.doWork()

        Truth.assertThat(result).isInstanceOf(ListenableWorker.Result.Failure::class.java)
    }
}

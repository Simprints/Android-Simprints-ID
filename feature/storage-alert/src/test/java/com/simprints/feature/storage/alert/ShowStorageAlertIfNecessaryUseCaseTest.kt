package com.simprints.feature.storage.alert

import android.content.Context
import android.os.StatFs
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.MINIMUM_FREE_SPACE_MB
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class ShowStorageAlertIfNecessaryUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var configRepo: ConfigRepository

    @MockK
    private lateinit var showStorageNotification: ShowStorageWarningNotificationUseCase

    private lateinit var useCase: ShowStorageAlertIfNecessaryUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkConstructor(StatFs::class)
        every { context.filesDir } returns File("/data/user/0/com.simprints.id/files")

        coEvery { configRepo.getProjectConfiguration() } returns mockk {
            every { custom } returns mapOf(MINIMUM_FREE_SPACE_MB to JsonPrimitive(20))
        }
        justRun { showStorageNotification.invoke() }

        useCase = ShowStorageAlertIfNecessaryUseCase(
            context = context,
            configRepo = configRepo,
            showStorageNotification = showStorageNotification,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @After
    fun tearDown() {
        unmockkConstructor(StatFs::class)
    }

    @Test
    fun `shows notification when available storage is below threshold`() = runTest {
        mockStorageStats(freeMb = 19L)

        useCase()

        verify(exactly = 1) { showStorageNotification() }
    }

    @Test
    fun `does not show notification when available storage equals the threshold`() = runTest {
        mockStorageStats(freeMb = 20L)

        useCase()

        verify(exactly = 0) { showStorageNotification() }
    }

    @Test
    fun `does not show notification when available storage is above threshold`() = runTest {
        mockStorageStats(freeMb = 21L)

        useCase()

        verify(exactly = 0) { showStorageNotification() }
    }

    private fun mockStorageStats(freeMb: Long) {
        every { anyConstructed<StatFs>().freeBytes } returns (freeMb * 1024 * 1024)
    }
}

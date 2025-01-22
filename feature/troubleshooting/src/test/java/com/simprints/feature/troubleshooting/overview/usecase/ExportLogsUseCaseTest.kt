package com.simprints.feature.troubleshooting.overview.usecase

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LogDirectoryProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import java.io.File
import java.text.SimpleDateFormat
import kotlin.test.Test

class ExportLogsUseCaseTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var logDirectoryProvider: LogDirectoryProvider

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    private lateinit var dateFormatter: SimpleDateFormat

    private lateinit var logFilesRoot: File
    private lateinit var cacheFileRoot: File

    private lateinit var exportLogsUseCase: ExportLogsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        logFilesRoot = File("logs")
        cacheFileRoot = File("cache")

        every { context.cacheDir } returns cacheFileRoot
        every { dateFormatter.format(any()) } returns "fileName"
        coEvery { configManager.getProjectConfiguration() } returns projectConfiguration

        exportLogsUseCase = ExportLogsUseCase(
            deviceId = "deviceId",
            context = context,
            logDirectoryProvider = logDirectoryProvider,
            configManager = configManager,
            dateFormatter = dateFormatter,
        )
    }

    @After
    fun tearDown() {
        logFilesRoot.deleteRecursively()
        cacheFileRoot.deleteRecursively()
    }

    @Test
    fun `skips zip creation if no files found`() = runTest {
        every { logDirectoryProvider.invoke(any()) } returns logFilesRoot

        val results = exportLogsUseCase.invoke().toList()
        assertThat(results).containsExactly(
            ExportLogsUseCase.LogsExportResult.InProgress,
            ExportLogsUseCase.LogsExportResult.Failed,
        )
    }

    @Test
    fun `if files are present, zip is created`() = runTest {
        every { logDirectoryProvider.invoke(any()) } returns logFilesRoot.also {
            it.mkdirs()
            File(it, "file1").createNewFile()
        }
        coEvery {
            projectConfiguration.general.settingsPassword.getNullablePassword()
        } returns "1234"

        val results = exportLogsUseCase.invoke().toList()
        assertThat(results).containsExactly(
            ExportLogsUseCase.LogsExportResult.InProgress,
            ExportLogsUseCase.LogsExportResult.Success("deviceId", File("cache/log_archives/fileName_deviceId.zip")),
        )

        coVerify(exactly = 0) { projectConfiguration.projectId }
    }

    @Test
    fun `existing zip file is deleted before creating new one`() = runTest {
        every { logDirectoryProvider.invoke(any()) } returns logFilesRoot.also {
            it.mkdirs()
            File(it, "file1").createNewFile()
        }
        File(cacheFileRoot, "log_archives").let {
            it.mkdirs()
            File(it, "oldArchive.zip").createNewFile()
        }

        coEvery {
            projectConfiguration.general.settingsPassword.getNullablePassword()
        } returns "1234"

        val results = exportLogsUseCase.invoke().toList()
        assertThat(results).containsExactly(
            ExportLogsUseCase.LogsExportResult.InProgress,
            ExportLogsUseCase.LogsExportResult.Success("deviceId", File("cache/log_archives/fileName_deviceId.zip")),
        )

        assertThat(cacheFileRoot.listFiles()?.none { it.name == "oldArchive.zip" }).isTrue()
    }

    @Test
    fun `if project does not have password, zip is created with project id`() = runTest {
        every { logDirectoryProvider.invoke(any()) } returns logFilesRoot.also {
            it.mkdirs()
            File(it, "file1").createNewFile()
        }
        coEvery {
            projectConfiguration.general.settingsPassword.getNullablePassword()
        } returns null
        coEvery { projectConfiguration.projectId } returns "projectId"

        val results = exportLogsUseCase.invoke().toList()
        assertThat(results).containsExactly(
            ExportLogsUseCase.LogsExportResult.InProgress,
            ExportLogsUseCase.LogsExportResult.Success("deviceId", File("cache/log_archives/fileName_deviceId.zip")),
        )

        coVerify(exactly = 1) { projectConfiguration.projectId }
    }
}

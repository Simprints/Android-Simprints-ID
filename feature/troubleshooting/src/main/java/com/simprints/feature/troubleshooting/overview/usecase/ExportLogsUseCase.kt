package com.simprints.feature.troubleshooting.overview.usecase

import android.content.Context
import com.simprints.feature.troubleshooting.FileNameDateTimeFormatter
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LogDirectoryProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

internal class ExportLogsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logDirectoryProvider: LogDirectoryProvider,
    private val configManager: ConfigManager,
    @FileNameDateTimeFormatter private val dateFormatter: SimpleDateFormat,
) {
    operator fun invoke(): Flow<LogsExportResult> = flow {
        emit(LogsExportResult.InProgress)
        val directory = logDirectoryProvider(context)
        val files = directory.listFiles().orEmpty()

        if (files.isNotEmpty()) {
            val archiveFile = createArchiveFile(dateFormatter.format(Date()))
            val password = getPassword()
            createZipArchiveWithPassword(archiveFile, files.toList(), password)

            delay(2000L) // To look like it does something
            emit(LogsExportResult.Success(archiveFile))
        } else {
            emit(LogsExportResult.NotStarted)
        }
    }

    private fun createArchiveFile(name: String): File = File(context.cacheDir, ARCHIVE_NAME)
        .also {
            if (it.exists()) {
                // Keep the archive cache slim
                it.deleteRecursively()
            }
            it.mkdirs()
        }.let { File(it, "$name.zip") }

    private suspend fun getPassword() = configManager
        .getProjectConfiguration()
        .let { it.general.settingsPassword.getNullablePassword() ?: it.projectId }
        .toCharArray()

    fun createZipArchiveWithPassword(
        archiveFile: File,
        contentFiles: List<File>,
        password: CharArray,
    ) {
        val zipParameters = ZipParameters().apply {
            isEncryptFiles = true
            encryptionMethod = EncryptionMethod.AES
        }
        val zipFile = ZipFile(archiveFile, password)
        zipFile.addFiles(contentFiles, zipParameters)
    }

    sealed class LogsExportResult {
        data class Success(
            val file: File,
        ) : LogsExportResult()

        data object InProgress : LogsExportResult()

        data object NotStarted : LogsExportResult()
    }

    companion object {
        private const val ARCHIVE_NAME = "log_archives/"
    }
}

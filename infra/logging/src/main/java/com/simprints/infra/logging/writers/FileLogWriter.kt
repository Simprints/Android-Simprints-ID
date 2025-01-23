package com.simprints.infra.logging.writers

import android.content.Context
import co.touchlab.kermit.io.RollingFileLogWriter
import co.touchlab.kermit.io.RollingFileLogWriterConfig
import com.simprints.infra.logging.LogDirectoryProvider
import kotlinx.io.files.Path

internal class FileLogWriter(
    context: Context,
    directoryProvider: LogDirectoryProvider = LogDirectoryProvider(),
) : RollingFileLogWriter(
        config = RollingFileLogWriterConfig(
            logFileName = "simprints",
            logFilePath = Path(directoryProvider(context).path),
            rollOnSize = 2 * 1024 * 1024, // 2MB
            maxLogFiles = 4,
        ),
    )

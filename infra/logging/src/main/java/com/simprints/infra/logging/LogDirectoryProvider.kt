package com.simprints.infra.logging

import android.content.Context
import java.io.File

class LogDirectoryProvider {
    operator fun invoke(context: Context): File = File(context.filesDir, "logs")
        .also { if (!it.exists()) it.mkdirs() }
}

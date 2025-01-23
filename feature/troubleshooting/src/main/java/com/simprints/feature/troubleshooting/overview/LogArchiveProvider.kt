package com.simprints.feature.troubleshooting.overview

import androidx.core.content.FileProvider

/**
 * Documentation recommends having a custom class that extends FileProvider to avoid crashes on some devices.
 */
internal class LogArchiveProvider : FileProvider()

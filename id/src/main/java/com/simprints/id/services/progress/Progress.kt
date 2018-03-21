package com.simprints.id.services.progress

open class Progress(open val currentValue: Int, open val maxValue: Int)
data class UploadProgress(override val currentValue: Int, override val maxValue: Int) : Progress(currentValue, maxValue)
data class DownloadProgress(override val currentValue: Int, override val maxValue: Int) : Progress(currentValue, maxValue)

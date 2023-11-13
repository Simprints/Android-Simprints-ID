package com.simprints.fingerprint.capture.models

import androidx.annotation.Keep
import com.simprints.moduleapi.common.IPath
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
internal class Path(override val parts: Array<String>) : IPath

package com.simprints.face.capture.models

import com.simprints.moduleapi.common.IPath
import kotlinx.parcelize.Parcelize

@Parcelize
internal class Path(override val parts: Array<String>) : IPath

internal fun IPath.fromModuleApiToDomain() = Path(parts)

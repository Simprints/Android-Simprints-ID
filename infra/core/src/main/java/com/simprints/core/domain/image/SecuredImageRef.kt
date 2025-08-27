package com.simprints.core.domain.image

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SecuredImageRef(
    val relativePath: Path,
) : Serializable

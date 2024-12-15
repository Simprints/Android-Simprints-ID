package com.simprints.infra.images.model

import androidx.annotation.Keep

@Keep
data class SecuredImageRef(
    override val relativePath: Path,
) : ImageRef(relativePath)

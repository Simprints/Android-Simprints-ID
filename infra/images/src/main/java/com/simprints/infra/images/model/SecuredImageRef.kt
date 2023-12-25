package com.simprints.infra.images.model

import com.google.errorprone.annotations.Keep

@Keep
data class SecuredImageRef(override val relativePath: Path) : ImageRef(relativePath)

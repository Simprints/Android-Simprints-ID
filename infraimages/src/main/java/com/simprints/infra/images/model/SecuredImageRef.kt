package com.simprints.infra.images.model

import kotlinx.parcelize.Parcelize

@Parcelize
data class SecuredImageRef(override val relativePath: Path) : ImageRef(relativePath)

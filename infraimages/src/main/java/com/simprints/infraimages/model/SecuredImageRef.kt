package com.simprints.infraimages.model

import kotlinx.parcelize.Parcelize

@Parcelize
data class SecuredImageRef(override val relativePath: Path) : ImageRef(relativePath)

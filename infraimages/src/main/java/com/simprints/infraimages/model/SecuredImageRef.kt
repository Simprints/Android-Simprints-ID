package com.simprints.infraimages.model

import com.simprints.infraimages.model.ImageRef
import com.simprints.infraimages.model.Path
import kotlinx.parcelize.Parcelize

@Parcelize
data class SecuredImageRef(override val relativePath: Path) : ImageRef(relativePath)

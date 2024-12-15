package com.simprints.infra.images.model

import java.io.Serializable

abstract class ImageRef(
    open val relativePath: Path,
) : Serializable

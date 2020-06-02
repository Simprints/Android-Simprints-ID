package com.simprints.id.data.images.model

import kotlinx.android.parcel.Parcelize

@Parcelize
data class SecuredImageRef(override val relativePath: Path) : ImageRef(relativePath)

package com.simprints.core.images.model

import kotlinx.android.parcel.Parcelize

@Parcelize
data class SecuredImageRef(override val path: Path) : ImageRef(path)

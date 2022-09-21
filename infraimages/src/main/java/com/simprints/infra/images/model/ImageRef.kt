package com.simprints.infra.images.model

import android.os.Parcelable

abstract class ImageRef(open val relativePath: Path) : Parcelable

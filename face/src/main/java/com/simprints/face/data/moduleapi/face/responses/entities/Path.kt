package com.simprints.face.data.moduleapi.face.responses.entities

import com.simprints.moduleapi.common.IPath
import kotlinx.android.parcel.Parcelize

@Parcelize
class Path(override val parts: Array<String>) : IPath

fun IPath.fromModuleApiToDomain() = Path(parts)

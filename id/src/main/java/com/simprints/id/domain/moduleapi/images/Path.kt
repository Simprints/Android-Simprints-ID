package com.simprints.id.domain.moduleapi.images

import com.simprints.core.images.model.Path
import com.simprints.moduleapi.common.IPath
import kotlinx.android.parcel.Parcelize

fun Path.fromDomainToModuleApi(): IPath = IPathImpl(parts)

fun IPath.fromModuleApiToDomain() = Path(parts)

@Parcelize
private class IPathImpl(override val parts: Array<String>) : IPath

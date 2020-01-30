package com.simprints.id.data.db.images.domain

import com.simprints.core.images.model.Path
import com.simprints.moduleapi.common.IPath
import kotlinx.android.parcel.Parcelize

// STOPSHIP TODO: confirm package
fun Path.fromDomainToModuleApi(): IPath = IPathImpl(parts)

fun IPath.fromModuleApiToDomain() = Path(parts)

@Parcelize
private class IPathImpl(override val parts: Array<String>) : IPath

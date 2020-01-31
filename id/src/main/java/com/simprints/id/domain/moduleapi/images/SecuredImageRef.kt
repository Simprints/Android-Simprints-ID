package com.simprints.id.domain.moduleapi.images

import com.simprints.core.images.model.SecuredImageRef
import com.simprints.moduleapi.common.IPath
import com.simprints.moduleapi.common.ISecuredImageRef
import kotlinx.android.parcel.Parcelize

fun SecuredImageRef.fromDomainToModuleApi(): ISecuredImageRef = ISecuredImageRefImpl(
    path.fromDomainToModuleApi()
)

fun ISecuredImageRef.fromModuleApiToDomain() = SecuredImageRef(path.fromModuleApiToDomain())

@Parcelize
private class ISecuredImageRefImpl(override val path: IPath): ISecuredImageRef

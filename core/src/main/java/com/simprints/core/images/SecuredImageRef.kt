package com.simprints.core.images

import com.simprints.moduleapi.common.IPath
import com.simprints.moduleapi.common.ISecuredImageRef
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SecuredImageRef(override val path: IPath) : ImageRef(path)

fun SecuredImageRef.fromDomainToModuleApi(): ISecuredImageRef = SecuredImageRefImpl(path)

@Parcelize
private class SecuredImageRefImpl(
    override val path: IPath
): ISecuredImageRef

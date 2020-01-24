package com.simprints.core.images

import android.os.Parcelable
import com.simprints.moduleapi.common.IPath
import com.simprints.moduleapi.common.ISecuredImageRef
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SecuredImageRef(
    override val relativePath: IPath,
    override val fullPath: String
) : ImageRef(relativePath, fullPath), Parcelable

fun SecuredImageRef.fromDomainToModuleApi(): ISecuredImageRef = SecuredImageRefImpl(
    relativePath, fullPath
)

@Parcelize
private class SecuredImageRefImpl(
    override val relativePath: IPath,
    override val fullPath: String
): ISecuredImageRef

package com.simprints.core.images

import android.os.Parcelable
import com.simprints.moduleapi.common.ISecuredImageRef
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SecuredImageRef(override val path: String) : ImageRef(path), Parcelable

fun SecuredImageRef.fromDomainToModuleApi(): ISecuredImageRef = SecuredImageRefImpl(path)



@Parcelize
private class SecuredImageRefImpl(override val path: String): ISecuredImageRef

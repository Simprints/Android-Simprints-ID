package com.simprints.core.images

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SecuredImageRef(override val uri: String) : ImageRef(uri), Parcelable

fun SecuredImageRef.fromDomainToModuleApi(): ISecuredImageRef = SecuredImageRefImpl(uri)



@Parcelize
private class SecuredImageRefImpl(override val uri: String): ISecuredImageRef

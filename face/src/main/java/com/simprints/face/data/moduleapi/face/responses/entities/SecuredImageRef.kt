package com.simprints.face.data.moduleapi.face.responses.entities

import com.simprints.moduleapi.common.ISecuredImageRef
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SecuredImageRef(override val path: String) : ISecuredImageRef

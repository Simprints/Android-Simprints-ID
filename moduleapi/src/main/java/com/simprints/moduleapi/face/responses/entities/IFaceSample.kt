package com.simprints.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.common.ISecuredImageRef

interface IFaceSample: Parcelable {
    val faceId: String
    val template: ByteArray
    val imageRef: ISecuredImageRef?
}

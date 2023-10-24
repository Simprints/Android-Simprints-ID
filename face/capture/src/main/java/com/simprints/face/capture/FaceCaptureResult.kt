package com.simprints.face.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FaceCaptureResult(
    val results: List<IFaceCaptureResult>,
) : Parcelable {

    @Keep
    @Parcelize
    data class Item(
        override val index: Int,
        override val sample: IFaceSample?,
    ) : IFaceCaptureResult, Parcelable

    @Keep
    @Parcelize
    data class Sample(
        override val faceId: String,
        override val template: ByteArray,
        override val imageRef: ISecuredImageRef?,
        override val format: String
    ) : IFaceSample, Parcelable
}

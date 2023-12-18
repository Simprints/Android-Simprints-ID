package com.simprints.face.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.infra.images.model.SecuredImageRef
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Keep
data class FaceCaptureResult(
    val results: List<Item>,
) : Serializable {

    @Keep
    @Parcelize
    data class Item(
        val index: Int,
        val sample: Sample?,
    ) : Parcelable

    @Keep
    @Parcelize
    data class Sample(
        val faceId: String,
        val template: ByteArray,
        val imageRef: SecuredImageRef?,
        val format: String,
    ) : Parcelable
}

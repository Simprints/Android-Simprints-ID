package com.simprints.fingerprint.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.images.model.SecuredImageRef
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Keep
data class FingerprintCaptureResult(
    var results: List<Item>,
) : Serializable {

    @Keep
    //TODO: This @Parcelize is not needed?
    @Parcelize
    data class Item(
        val identifier: IFingerIdentifier,
        val sample: Sample?,
    ) : Parcelable

    @Keep
    @Parcelize
    data class Sample(
        val fingerIdentifier: IFingerIdentifier,
        val template: ByteArray,
        val templateQualityScore: Int,
        val imageRef: SecuredImageRef?,
        val format: String,
    ) : Parcelable

}

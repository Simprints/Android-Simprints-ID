package com.simprints.fingerprint.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.moduleapi.common.IPath
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import kotlinx.parcelize.Parcelize
import java.nio.file.Path

@Keep
@Parcelize
data class FingerprintCaptureResult(
    var results: List<IFingerprintCaptureResult>,
) : Parcelable {

    @Keep
    @Parcelize
    data class Item(
        override val identifier: IFingerIdentifier,
        override val sample: IFingerprintSample?,
    ) : IFingerprintCaptureResult, Parcelable

    @Keep
    @Parcelize
    data class Sample(
        override val fingerIdentifier: IFingerIdentifier,
        override val template: ByteArray,
        override val templateQualityScore: Int,
        override val imageRef: ISecuredImageRef?,
        override val format: String
    ) : IFingerprintSample, Parcelable

    @Keep
    @Parcelize
    data class SecuredImageRef(override val path: Path) : ISecuredImageRef

    @Keep
    @Parcelize
    class Path(override val parts: Array<String>) : IPath

}

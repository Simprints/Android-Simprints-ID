package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import com.simprints.core.images.fromDomainToModuleApi
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.requests.IFingerprintSample
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class FingerprintSample(
    open val fingerIdentifier: FingerIdentifier,
    open val template: ByteArray,
    open val templateQualityScore: Int,
    open val imageRef: SecuredImageRef? = null) : Parcelable {

    @IgnoredOnParcel
    open val id: String by lazy {
        UUID.nameUUIDFromBytes(template).toString()
    }
}


fun FingerprintSample.fromDomainToModuleApi(): IFingerprintSample =
    FingerprintSampleImpl(id, fingerIdentifier.fromDomainToModuleApi(), template, templateQualityScore, imageRef?.fromDomainToModuleApi())


@Parcelize
private class FingerprintSampleImpl(
    override val id: String,
    override val fingerIdentifier: IFingerIdentifier,
    override val template: ByteArray,
    override val templateQualityScore: Int,
    override val imageRef: ISecuredImageRef?) : IFingerprintSample, Parcelable

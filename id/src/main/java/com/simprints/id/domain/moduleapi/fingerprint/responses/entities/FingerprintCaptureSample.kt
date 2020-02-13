package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.core.images.model.SecuredImageRef
import com.simprints.id.domain.moduleapi.images.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.images.fromModuleApiToDomain
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.fromDomainToModuleApi
import com.simprints.id.data.db.person.domain.fromModuleApiToDomain
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class FingerprintCaptureSample(
    open val fingerIdentifier: FingerIdentifier,
    open val template: ByteArray,
    open val templateQualityScore: Int,
    open val imageRef: SecuredImageRef? = null) : Parcelable {

    @IgnoredOnParcel
    open val id: String by lazy {
        UUID.nameUUIDFromBytes(template).toString()
    }
}

fun FingerprintCaptureSample.fromDomainToModuleApi(): IFingerprintSample {
    return FingerprintCaptureSampleImpl(
        fingerIdentifier.fromDomainToModuleApi(),
        template,
        templateQualityScore,
        imageRef?.fromDomainToModuleApi()
    )
}

fun IFingerprintSample.fromModuleApiToDomain(): FingerprintCaptureSample {
    return FingerprintCaptureSample(
        fingerIdentifier.fromModuleApiToDomain(),
        template,
        templateQualityScore,
        imageRef?.fromModuleApiToDomain()
    )
}

@Parcelize
private class FingerprintCaptureSampleImpl(
    override val fingerIdentifier: IFingerIdentifier,
    override val template: ByteArray,
    override val templateQualityScore: Int,
    override val imageRef: ISecuredImageRef?) : IFingerprintSample, Parcelable

package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.fingerprint.models.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.fingerprint.models.fromModuleApiToDomain
import com.simprints.id.domain.moduleapi.images.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.images.fromModuleApiToDomain
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
open class FingerprintCaptureSample(
    open val fingerIdentifier: Finger,
    open val template: ByteArray,
    open val templateQualityScore: Int,
    val format: String,
    open val imageRef: SecuredImageRef? = null
) : Parcelable {

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
        format,
        imageRef?.fromDomainToModuleApi()
    )
}

fun IFingerprintSample.fromModuleApiToDomain(): FingerprintCaptureSample {
    return FingerprintCaptureSample(
        fingerIdentifier.fromModuleApiToDomain(),
        template,
        templateQualityScore,
        format,
        imageRef?.fromModuleApiToDomain()
    )
}

@Parcelize
private class FingerprintCaptureSampleImpl(
    override val fingerIdentifier: IFingerIdentifier,
    override val template: ByteArray,
    override val templateQualityScore: Int,
    override val format: String,
    override val imageRef: ISecuredImageRef?
) : IFingerprintSample, Parcelable

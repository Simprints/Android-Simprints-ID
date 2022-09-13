package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.domain.models.fingerprint.fromModuleApiToDomain
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.db.subject.domain.fromDomainToModuleApi
import com.simprints.id.data.db.subject.domain.fromModuleApiToDomain
import com.simprints.id.domain.moduleapi.images.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.images.fromModuleApiToDomain
import com.simprints.infraimages.model.SecuredImageRef
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
open class FingerprintCaptureSample(
    open val fingerIdentifier: FingerIdentifier,
    open val template: ByteArray,
    open val templateQualityScore: Int,
    val format: FingerprintTemplateFormat,
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
        format.fromDomainToModuleApi(),
        imageRef?.fromDomainToModuleApi()
    )
}

fun IFingerprintSample.fromModuleApiToDomain(): FingerprintCaptureSample {
    return FingerprintCaptureSample(
        fingerIdentifier.fromModuleApiToDomain(),
        template,
        templateQualityScore,
        format.fromModuleApiToDomain(),
        imageRef?.fromModuleApiToDomain()
    )
}

@Parcelize
private class FingerprintCaptureSampleImpl(
    override val fingerIdentifier: IFingerIdentifier,
    override val template: ByteArray,
    override val templateQualityScore: Int,
    override val format: IFingerprintTemplateFormat,
    override val imageRef: ISecuredImageRef?
) : IFingerprintSample, Parcelable

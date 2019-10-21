package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import com.simprints.core.images.fromDomainToModuleApi
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class FaceSample(
    open val template: ByteArray,
    open val imageRef: SecuredImageRef?) : Parcelable {

    @IgnoredOnParcel
    open val id: String by lazy {
        UUID.nameUUIDFromBytes(template).toString()
    }
}

fun FaceSample.fromDomainToModuleApi(): IFaceSample =
    IFaceSampleImpl(id, template, imageRef?.fromDomainToModuleApi())

@Parcelize
private class IFaceSampleImpl(
    override val faceId: String,
    override val template: ByteArray,
    override val imageRef: ISecuredImageRef?
): IFaceSample

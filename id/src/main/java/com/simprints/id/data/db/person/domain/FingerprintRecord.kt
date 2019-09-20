package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import kotlinx.android.parcel.Parcelize

// FingerprintRecord = FingerprintSample + patient Id (it will be used for matchings)
@Parcelize
class FingerprintRecord(val personId: String,
                        override val fingerIdentifier: FingerIdentifier,
                        override val template: ByteArray,
                        override val imageRef: SecuredImageRef?,
                        override val templateQualityScore: Int) : FingerprintSample(fingerIdentifier, template, templateQualityScore, imageRef), Parcelable

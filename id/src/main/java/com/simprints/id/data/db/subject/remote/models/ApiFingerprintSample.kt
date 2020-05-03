package com.simprints.id.data.db.subject.remote.models

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.db.subject.domain.FingerprintSample

@Keep
data class ApiFingerprintSample(var finger: FingerIdentifier,
                                val template: String,
                                val quality: Int) {

    constructor (sample: FingerprintSample) : this(
        finger = FingerIdentifier.valueOf(sample.fingerIdentifier.name),
        template = EncodingUtils.byteArrayToBase64(sample.template),
        quality = sample.templateQualityScore)
}

fun FingerprintSample.fromDomainToApi(): ApiFingerprintSample =
    ApiFingerprintSample(
        finger = fingerIdentifier,
        template = EncodingUtils.byteArrayToBase64(template), // TODO: build rid of double bang
        quality = templateQualityScore
    )

fun ApiFingerprintSample.fromApiToDomain(): FingerprintSample =
    FingerprintSample(
        fingerIdentifier = finger,
        template = EncodingUtils.base64ToBytes(template),
        templateQualityScore = quality)

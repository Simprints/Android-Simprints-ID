package com.simprints.id.data.db.event.remote.events.subject

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.subject.domain.FaceSample

@Keep
data class ApiFaceSample(val template: String = "")

fun FaceSample.fromDomainToApi(): ApiFaceSample {
    val templateStr = EncodingUtils.byteArrayToBase64(template)
    return ApiFaceSample(templateStr)
}

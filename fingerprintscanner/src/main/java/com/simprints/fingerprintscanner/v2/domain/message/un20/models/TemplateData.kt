package com.simprints.fingerprintscanner.v2.domain.message.un20.models

class TemplateData(
    val templateType: TemplateType,
    val quality: Int,
    val template: ByteArray
)

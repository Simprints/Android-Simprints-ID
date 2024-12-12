package com.simprints.fingerprint.infra.simafiswrapper.models

import java.nio.ByteBuffer

object TemplateGenerator {
    val validTemplate: ByteArray = createValidTemplate()
    val validTemplateWithLowQuality: ByteArray = createValidTemplate(50)

    fun createValidTemplate(quality: Int = 100): ByteArray {
        val template = ByteBuffer.allocate(30)
        template.putInt(FORMAT_ID, ISO_FORMAT_ID)
        template.putInt(VERSION, ISO_2005_VERSION)
        template.putInt(RECORD_LENGTH, 30)
        template.put(NB_FINGERPRINTS, 1.toByte())
        template.put(FIRST_QUALITY, quality.toByte())
        return template.array()
    }

    const val ISO_FORMAT_ID = 0x464D5200 // 'F' 'M' 'R' 00hex
    const val ISO_2005_VERSION = 0x20323000 // ' ' '2' '0' 00hex
    const val FORMAT_ID = 0 // INT
    const val VERSION = 4 // INT
    const val RECORD_LENGTH = 8 // INT
    const val NB_FINGERPRINTS = 22 // BYTE
    const val FIRST_QUALITY = 26 // BYTE
}

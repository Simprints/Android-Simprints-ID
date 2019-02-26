package com.simprints.id.domain.fingerprint

import android.os.Parcelable
import com.simprints.id.FingerIdentifier
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Parcelize
data class Fingerprint(
    val fingerId: FingerIdentifier,
    private val template: @RawValue ByteBuffer) : Parcelable {

    /**
     * @return A newly allocated byte array containing the ISO 2005 template of
     * the fingerprint
     */
    val templateBytes: ByteArray
        get() {
            template.position(0)
            val templateBytes = ByteArray(template.remaining())
            template.get(templateBytes)
            return templateBytes
        }

    /**
     * @return The quality score of this fingerprint, as stored in its template
     */
    val qualityScore: Int
        get() = template.get(FIRST_QUALITY).toInt()

    /**
     * ISO 2005 byte array constructor
     *
     * @param fingerId         Finger identifier of the fingerprint
     * @param isoTemplateBytes Byte array containing an ISO 2005 fingerprint template
     * @throws IllegalArgumentException If the bytes array specified is not a valid ISO 2005
     * (2011 not supported yet) template containing only 1 fingerprint.
     */
    @Throws(IllegalArgumentException::class)
    constructor(fingerId: FingerIdentifier, isoTemplateBytes: ByteArray) :
        this(fingerId, ByteBuffer.allocateDirect(isoTemplateBytes.size)) {

        template.order(ByteOrder.BIG_ENDIAN)
        try {
            // Checks the format identifier
            if (this.template.getInt(FORMAT_ID) != ISO_FORMAT_ID) {
                throw IllegalArgumentException("Invalid template: not an ISO template")
            }

            // Checks the ISO version
            if (this.template.getInt(VERSION) != ISO_2005_VERSION) {
                throw IllegalArgumentException("Invalid template: only ISO 2005 is supported")
            }

            // Checks the length of the record
            if (this.template.getInt(RECORD_LENGTH) != isoTemplateBytes.size) {
                throw IllegalArgumentException("Invalid template: invalid length")
            }

            // Checks the number of fingers
            if (this.template.get(NB_FINGERPRINTS).toInt() != 1) {
                throw IllegalArgumentException("Invalid template: only single fingerprint template ares supported")
            }
        } catch (ex: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Invalid template: Processing byte[] failed")
        }

    }

    companion object {

        private val ISO_FORMAT_ID = Integer.parseInt("464D5200", 16)     // 'F' 'M' 'R' 00hex
        private val ISO_2005_VERSION = Integer.parseInt("20323000", 16)  // ' ' '2' '0' 00hex
        private const val FORMAT_ID = 0              // INT
        private const val VERSION = 4                // INT
        private const val RECORD_LENGTH = 8          // INT
        private const val NB_FINGERPRINTS = 22       // BYTE
        private const val FIRST_QUALITY = 26         // BYTE
    }
}

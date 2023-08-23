package com.simprints.fingerprint.data.domain.fingerprint

import android.os.Parcel
import android.os.Parcelable
import com.simprints.fingerprint.data.domain.images.FingerprintImageRef
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * This class represents the data from a captured fingerprint biometrics
 *
 * @property fingerId  the identifier of the captured fingerprint
 * @property template  the generated biometric template representing the fingerprint signature
 * @property imageRef  the fingerprint image that was captured to generate the template
 * @property format  the format for the template (i.e. the format used to generate the template which will determine the format used to match fingerprints)
 */
class Fingerprint(
    val fingerId: FingerIdentifier,
    val template: ByteBuffer,
    var imageRef: FingerprintImageRef? = null,
    val format: String
) : Parcelable {

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
     * @param format           The format of the template
     * @throws IllegalArgumentException If the bytes array specified is not a valid ISO 2005
     * (2011 not supported yet) template containing only 1 fingerprint.
     */
    @Throws(IllegalArgumentException::class)
    constructor(fingerId: FingerIdentifier, isoTemplateBytes: ByteArray, format: String) : this(
        fingerId,
        ByteBuffer.allocateDirect(isoTemplateBytes.size),
        format = format
    ) {

        template.put(isoTemplateBytes)
        template.order(ByteOrder.BIG_ENDIAN)

        require(this.template.getInt(FORMAT_ID) == ISO_FORMAT_ID) { "Invalid template: not an ISO template" }
        require(this.template.getInt(VERSION) == ISO_2005_VERSION) { "Invalid template: only ISO 2005 is supported" }
        require(this.template.getInt(RECORD_LENGTH) == isoTemplateBytes.size) { "Invalid template: invalid length" }
        require(this.template.get(NB_FINGERPRINTS) == 1.toByte()) { "Invalid template: only single fingerprint template ares supported" }
    }
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.run {
            writeInt(fingerId.ordinal)
            val bytes = templateBytes
            writeInt(bytes.size)
            writeByteArray(bytes)
            writeString(format)
        }
    }

    companion object CREATOR : Parcelable.Creator<Fingerprint> {

        private val ISO_FORMAT_ID = Integer.parseInt("464D5200", 16)     // 'F' 'M' 'R' 00hex
        private val ISO_2005_VERSION = Integer.parseInt("20323000", 16)  // ' ' '2' '0' 00hex
        private const val FORMAT_ID = 0              // INT
        private const val VERSION = 4                // INT
        private const val RECORD_LENGTH = 8          // INT
        private const val NB_FINGERPRINTS = 22       // BYTE
        private const val FIRST_QUALITY = 26         // BYTE

        override fun createFromParcel(source: Parcel): Fingerprint {
            val fingerId = FingerIdentifier.values()[source.readInt()]
            val temp = ByteArray(source.readInt())
            source.readByteArray(temp)
            val template = ByteBuffer.allocateDirect(temp.size)
            template.put(temp)
            val format = source.readString()!!
            return Fingerprint(fingerId, template, format = format)
        }
        override fun newArray(size: Int): Array<Fingerprint?> = arrayOfNulls(size)
    }
}

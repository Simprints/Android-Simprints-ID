package com.simprints.id.commontesttools

import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.Fingerprint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

object FingerprintGeneratorUtils {

    private val ISO_FORMAT_ID = Integer.parseInt("464D5200", 16)     // 'F' 'M' 'R' 00hex
    private val ISO_2005_VERSION = Integer.parseInt("20323000", 16)  // ' ' '2' '0' 00hex
    private const val SECUGEN_HAMSTER_WIDTH: Short = 300
    private const val SECUGEN_HAMSTER_HEIGHT: Short = 400
    private const val SECUGEN_HAMSTER_PPCM: Short = 500


    private const val HEADER_SIZE = 28
    private const val MINUTIAE_SIZE = 6

    private const val FORMAT_ID = 0              // INT
    private const val VERSION = 4                // INT
    private const val RECORD_LENGTH = 8          // INT
    private const val WIDTH = 14                 // SHORT
    private const val HEIGHT = 16                // SHORT
    private const val HORIZONTAL_PPCM = 18       // SHORT
    private const val VERTICAL_PPCM = 20         // SHORT
    private const val NB_FINGERPRINTS = 22       // BYTE
    private const val FIRST_FINGER_POSITION = 24 // BYTE
    private const val FIRST_QUALITY = 26         // BYTE
    private const val FIRST_NB_MINUTIAE = 27     // BYTE
    private const val FIRST_MINUTIAE_START = 28

    private const val TYPE_AND_X_SHIFT = 0       // SHORT
    private const val ZEROS_AND_Y_SHIFT = 2      // SHORT
    private const val ANGLE_SHIFT = 4            // SHORT
    private const val QUALITY_SHIFT = 5          // SHORT

    private val RANDOM_GENERATOR = Random()
    private val FINGER_IDENTIFIERS = FingerIdentifier.values()


    /**
     * @return A random valid [Fingerprint] with a random [FingerIdentifier]
     */
    fun generateRandomFingerprint(): Fingerprint {
        val fingerNo = RANDOM_GENERATOR.nextInt(FINGER_IDENTIFIERS.size)
        val fingerId = FINGER_IDENTIFIERS[fingerNo]
        return generateRandomFingerprint(fingerId)
    }

    /**
     * @param fingerId Finger identifier of the fingerprint
     * @return A random valid [Fingerprint] with specified [FingerIdentifier]
     */
    fun generateRandomFingerprint(fingerId: FingerIdentifier): Fingerprint {
        val qualityScore = RANDOM_GENERATOR.nextInt(101).toByte()
        return generateRandomFingerprint(fingerId, qualityScore)
    }


    /**
     * @param fingerId     Finger identifier of the fingerprint
     * @param qualityScore Quality score of the fingerprint
     * @return A random valid [Fingerprint] with specified [FingerIdentifier]
     */
    fun generateRandomFingerprint(fingerId: FingerIdentifier,
                                  qualityScore: Byte): Fingerprint {
        val nbMinutiae = RANDOM_GENERATOR.nextInt(128).toByte()
        val length = HEADER_SIZE + nbMinutiae * MINUTIAE_SIZE

        val bb = ByteBuffer.allocateDirect(length)
        bb.order(ByteOrder.BIG_ENDIAN)

        bb.putInt(FORMAT_ID, ISO_FORMAT_ID)
        bb.putInt(VERSION, ISO_2005_VERSION)
        bb.putInt(RECORD_LENGTH, length)
        bb.putShort(WIDTH, SECUGEN_HAMSTER_WIDTH)
        bb.putShort(HEIGHT, SECUGEN_HAMSTER_HEIGHT)
        bb.putShort(HORIZONTAL_PPCM, SECUGEN_HAMSTER_PPCM)
        bb.putShort(VERTICAL_PPCM, SECUGEN_HAMSTER_PPCM)
        bb.put(NB_FINGERPRINTS, 1.toByte())
        bb.put(FIRST_FINGER_POSITION, 0.toByte())
        bb.put(FIRST_QUALITY, qualityScore)
        bb.put(FIRST_NB_MINUTIAE, nbMinutiae)

        for (minutiaNo in 0 until nbMinutiae) {
            val type = (RANDOM_GENERATOR.nextInt(3) shl 14).toShort()
            val x = RANDOM_GENERATOR.nextInt(SECUGEN_HAMSTER_WIDTH.toInt()).toShort()
            val y = RANDOM_GENERATOR.nextInt(SECUGEN_HAMSTER_HEIGHT.toInt()).toShort()
            val angle = RANDOM_GENERATOR.nextInt(256).toByte()
            val quality = RANDOM_GENERATOR.nextInt(101).toByte()
            bb.putShort(FIRST_MINUTIAE_START + minutiaNo * MINUTIAE_SIZE + TYPE_AND_X_SHIFT, (type + x).toShort())
            bb.putShort(FIRST_MINUTIAE_START + minutiaNo * MINUTIAE_SIZE + ZEROS_AND_Y_SHIFT, y)
            bb.put(FIRST_MINUTIAE_START + minutiaNo * MINUTIAE_SIZE + ANGLE_SHIFT, angle)
            bb.put(FIRST_MINUTIAE_START + minutiaNo * MINUTIAE_SIZE + QUALITY_SHIFT, quality)
        }

        bb.position(0)
        val templateBytes = ByteArray(bb.remaining())
        bb.get(templateBytes)
        return Fingerprint(fingerId, templateBytes)
    }
}

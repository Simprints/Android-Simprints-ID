package com.simprints.fingerprint.infra.matcher.algorithms.simafis

import com.simprints.fingerprint.infra.matcher.algorithms.simafis.models.SimAfisPerson
import java.nio.ByteBuffer

interface JNILibAfisInterface {
    fun nativeInit(): Boolean
    fun getNbCores(): Int
    fun verify(probe: ByteBuffer, candidate: ByteBuffer): Float
    fun identify(
        probe: SimAfisPerson,
        candidates: List<SimAfisPerson>,
        nbThreads: Int
    ): FloatArray
}

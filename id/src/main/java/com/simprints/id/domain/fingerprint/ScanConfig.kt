package com.simprints.id.domain.fingerprint


import com.simprints.id.FingerIdentifier

class ScanConfig {

    val fingerConfigs: Map<FingerIdentifier, ScanConfigFingerEntry> =
        FingerIdentifier.values().associate { Pair(it, ScanConfigFingerEntry()) }

    operator fun get(id: FingerIdentifier): FingerConfig =
        fingerConfigs[id]?.let { it.config }
            ?: throw IllegalArgumentException("No FingerConfig associated to id")

    fun getPriority(id: FingerIdentifier): Int =
        fingerConfigs[id]?.let { it.priority }
            ?: throw IllegalArgumentException("No priority associated to id")

    fun getOrder(id: FingerIdentifier): Int =
        fingerConfigs[id]?.let { it.order }
            ?: throw IllegalArgumentException("No priority associated to id")
}

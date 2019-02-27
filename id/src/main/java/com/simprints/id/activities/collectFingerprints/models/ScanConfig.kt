package com.simprints.id.activities.collectFingerprints.models


import com.simprints.id.FingerIdentifier

class ScanConfig {

    val fingerConfigs: Map<FingerIdentifier, ScanConfigFingerEntry> =
        FingerIdentifier.values().associate { Pair(it, ScanConfigFingerEntry()) }

    operator fun get(id: FingerIdentifier): FingerConfig =
        fingerConfigs[id]?.config
            ?: throw IllegalArgumentException("No FingerConfig associated to id")

    fun getPriority(id: FingerIdentifier): Int =
        fingerConfigs[id]?.priority
            ?: throw IllegalArgumentException("No priority associated to id")

    fun getOrder(id: FingerIdentifier): Int =
        fingerConfigs[id]?.order
            ?: throw IllegalArgumentException("No priority associated to id")
}

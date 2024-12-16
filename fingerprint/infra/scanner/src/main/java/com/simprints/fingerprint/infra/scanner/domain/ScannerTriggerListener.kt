package com.simprints.fingerprint.infra.scanner.domain

/**
 * This interface represents a callback that will be triggered when the scan button is clicked on
 * either the vero 1 or 2 scanners.
 */
interface ScannerTriggerListener {
    fun onTrigger()

    companion object {
        /**
         * For SAM construction : https://youtrack.jetbrains.com/issue/KT-7770
         * No longer needed upon update to Kotlin 1.4
         */
        inline operator fun invoke(crossinline op: () -> Unit) = object : ScannerTriggerListener {
            override fun onTrigger() {
                op()
            }
        }
    }
}

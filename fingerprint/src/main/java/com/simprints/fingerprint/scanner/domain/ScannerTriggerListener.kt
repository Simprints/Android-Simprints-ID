package com.simprints.fingerprint.scanner.domain

interface ScannerTriggerListener {

    fun onTrigger()

    companion object {

        /**
         * For SAM construction : https://youtrack.jetbrains.com/issue/KT-7770
         * No longer needed upon update to Kotlin 1.4
         */
        inline operator fun invoke(crossinline op: () -> Unit) =
            object : ScannerTriggerListener {
                override fun onTrigger() {
                    op()
                }
            }
    }
}

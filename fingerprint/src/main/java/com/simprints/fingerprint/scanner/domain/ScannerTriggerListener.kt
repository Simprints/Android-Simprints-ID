package com.simprints.fingerprint.scanner.domain

interface ScannerTriggerListener {

    fun onTrigger()

    companion object {

        // For SAM construction : https://youtrack.jetbrains.com/issue/KT-7770
        inline operator fun invoke(crossinline op: () -> Unit) =
            object : ScannerTriggerListener {
                override fun onTrigger() {
                    op()
                }
            }
    }
}

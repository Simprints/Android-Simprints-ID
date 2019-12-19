package com.simprints.fingerprintscannermock.simulated.common

data class ScannerState(var isUn20On: Boolean = false,
                        val eventQueue: MutableList<SimulatedScanner.() -> Unit> = mutableListOf())

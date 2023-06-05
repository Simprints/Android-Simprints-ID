package com.simprints.fingerprintscannermock.simulated.common

abstract class SimulatedScannerState(val eventQueue: MutableList<SimulatedScanner.() -> Unit> = mutableListOf())

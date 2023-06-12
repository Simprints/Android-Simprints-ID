package com.simprints.fingerprint.scannermock.simulated.common

abstract class SimulatedScannerState(val eventQueue: MutableList<SimulatedScanner.() -> Unit> = mutableListOf())

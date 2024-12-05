package com.simprints.fingerprint.infra.scannermock.simulated.common

abstract class SimulatedScannerState(val eventQueue: MutableList<SimulatedScanner.() -> Unit> = mutableListOf())

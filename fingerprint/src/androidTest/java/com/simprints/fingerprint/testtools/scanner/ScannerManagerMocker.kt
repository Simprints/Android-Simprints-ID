package com.simprints.fingerprint.testtools.scanner

import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprintscanner.Scanner
import com.simprints.testtools.common.syntax.whenThis

fun ScannerManager.setupScannerManagerMockWithMockedScanner(mockedScanner: Scanner = createMockedScanner()) {
    whenThis { scanner } thenReturn mockedScanner
}

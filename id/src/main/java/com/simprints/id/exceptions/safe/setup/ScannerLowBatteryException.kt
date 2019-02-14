package com.simprints.id.exceptions.safe.setup

import com.simprints.id.exceptions.safe.SafeException

class ScannerLowBatteryException(message: String = "ScannerLowBatteryException") :
    SafeException(message)

package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState
import com.simprints.fingerprintscannermock.simulated.common.SimulatedScannerState

class SimulatedScannerStateV2(var isUn20On: Boolean = false,
                              var isTriggerButtonActive: Boolean = true,
                              var smileLedState: SmileLedState = SmileLedState(
                                  LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                                  LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                                  LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                                  LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                                  LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)),
                              var batteryPercentCharge: Int = 80) : SimulatedScannerState()

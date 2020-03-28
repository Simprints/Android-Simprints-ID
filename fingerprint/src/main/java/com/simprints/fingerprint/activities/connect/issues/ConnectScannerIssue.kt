package com.simprints.fingerprint.activities.connect.issues

import androidx.annotation.IdRes
import com.simprints.fingerprint.R

enum class ConnectScannerIssue(@IdRes val navActionId: Int) {
    BLUETOOTH_OFF(R.id.action_connectScannerMainFragment_to_bluetoothOffFragment),
    NFC_OFF(R.id.action_connectScannerMainFragment_to_nfcOffFragment),
    NFC_PAIR(R.id.action_connectScannerMainFragment_to_nfcPairFragment),
    SERIAL_ENTRY_PAIR(R.id.action_connectScannerMainFragment_to_serialEntryFragment),
    TURN_ON_SCANNER(R.id.action_connectScannerMainFragment_to_turnOnScannerFragment)
}

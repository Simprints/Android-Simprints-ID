package com.simprints.fingerprint.scanner.old

import android.annotation.SuppressLint
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.exceptions.safe.scanner.BluetoothNotEnabledException
import com.simprints.fingerprint.exceptions.safe.scanner.MultipleScannersPairedException
import com.simprints.fingerprint.exceptions.safe.scanner.ScannerLowBatteryException
import com.simprints.fingerprint.exceptions.safe.scanner.ScannerNotPairedException
import com.simprints.fingerprint.exceptions.unexpected.scanner.BluetoothNotSupportedException
import com.simprints.fingerprint.exceptions.unexpected.scanner.NullScannerException
import com.simprints.fingerprint.exceptions.unexpected.scanner.UnknownScannerIssueException
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR
import com.simprints.fingerprintscanner.v1.Scanner
import com.simprints.fingerprintscanner.v1.ScannerCallback
import com.simprints.fingerprintscanner.v1.ScannerUtils
import com.simprints.fingerprintscanner.api.bluetooth.BluetoothComponentAdapter
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable

@Deprecated("Use new ScannerManager")
open class ScannerManagerImpl(private val bluetoothAdapter: BluetoothComponentAdapter) : ScannerManager {

    override var scanner: Scanner? = null
    override var lastPairedScannerId: String? = null

    @SuppressLint("CheckResult")
    override fun start(): Completable =
        disconnectVero()
            .andThen(checkBluetoothStatus())
            .andThen(initVero())
            .andThen(connectToVero())
            .andThen(resetVeroUI())
            .andThen(shutdownVero())
            .andThen(wakeUpVero())
            .trace("scannerSetup")

    override fun disconnectVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onComplete()
        } else {
            scanner?.disconnect(WrapperScannerCallback({
                result.onComplete()
            }, {
                result.onComplete()
            }))
        }
    }

    override fun checkBluetoothStatus(): Completable = Completable.create {
        if(!bluetoothIsEnabled()) {
            it.onError(BluetoothNotEnabledException())
        } else {
            it.onComplete()
        }
    }

    private fun bluetoothIsEnabled() = ScannerUtils.isBluetoothEnabled(bluetoothAdapter)

    override fun initVero(): Completable = Completable.create {
        val pairedScanners = ScannerUtils.getPairedScanners(bluetoothAdapter)
        when {
            pairedScanners.isEmpty() -> it.onError(ScannerNotPairedException())
            pairedScanners.size > 1 -> it.onError(MultipleScannersPairedException())
            else -> {
                val macAddress = pairedScanners[0]
                scanner = Scanner(macAddress, bluetoothAdapter)
                this.lastPairedScannerId = ScannerUtils.convertAddressToSerial(macAddress)
                it.onComplete()
            }
        }
    }


    override fun connectToVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onError(NullScannerException())
        } else {
            scanner?.connect(WrapperScannerCallback({
                result.onComplete()
            }, { scannerError ->
                scannerError?.let {
                    val issue = when (scannerError) {
                        SCANNER_ERROR.BLUETOOTH_DISABLED -> BluetoothNotEnabledException()
                        SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED -> BluetoothNotSupportedException()
                        SCANNER_ERROR.SCANNER_UNBONDED -> ScannerNotPairedException()
                        SCANNER_ERROR.BUSY, SCANNER_ERROR.IO_ERROR -> UnknownScannerIssueException.forScannerError(scannerError)
                        else -> UnknownScannerIssueException.forScannerError(scannerError)
                    }
                    result.onError(issue)
                } ?: result.onComplete()
            }))
        }
    }

    override fun wakeUpVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onError(NullScannerException())
        } else {
            scanner?.un20Wakeup(WrapperScannerCallback({
                result.onComplete()
            }, { scannerError ->

                scannerError?.let {
                    val issue = when (scannerError) {
                        SCANNER_ERROR.UN20_LOW_VOLTAGE -> {
                            ScannerLowBatteryException()
                        }
                        else -> UnknownScannerIssueException.forScannerError(scannerError)
                    }
                    result.onError(issue)
                } ?: result.onComplete()
            }
            ))
        }
    }

    override fun shutdownVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onError(NullScannerException())
        } else {
            scanner?.un20Shutdown(WrapperScannerCallback({
                result.onComplete()
            }, {
                result.onError(UnknownScannerIssueException.forScannerError(it))
            }))
        }
    }

    override fun resetVeroUI(): Completable = Completable.create { result ->

        if (scanner == null) {
            result.onError(NullScannerException())
        } else {
            scanner?.resetUI(WrapperScannerCallback({
                result.onComplete()
            }, { scannerError ->
                scannerError?.let {
                    result.onError(UnknownScannerIssueException.forScannerError(it))
                } ?: result.onComplete()
            }))
        }
    }

    override fun getAlertType(it: Throwable): FingerprintAlert =
        when (it) {
            is BluetoothNotEnabledException -> BLUETOOTH_NOT_ENABLED
            is BluetoothNotSupportedException -> BLUETOOTH_NOT_SUPPORTED
            is MultipleScannersPairedException -> MULTIPLE_PAIRED_SCANNERS
            is ScannerLowBatteryException -> LOW_BATTERY
            is ScannerNotPairedException -> NOT_PAIRED
            is UnknownScannerIssueException -> DISCONNECTED
            else -> UNEXPECTED_ERROR
        }

    override fun disconnectScannerIfNeeded() {
        scanner?.disconnect(object : ScannerCallback {
            override fun onSuccess() {}
            override fun onFailure(scanner_error: SCANNER_ERROR) {}
        })
    }

    private class WrapperScannerCallback(val success: () -> Unit, val failure: (scannerError: SCANNER_ERROR?) -> Unit) : ScannerCallback {
        override fun onSuccess() {
            success()
        }

        override fun onFailure(error: SCANNER_ERROR?) {
            failure(error)
        }
    }
}

package com.simprints.fingerprint.scanner

import android.annotation.SuppressLint
import com.simprints.fingerprint.data.domain.alert.Alert
import com.simprints.fingerprintscanner.SCANNER_ERROR
import com.simprints.fingerprintscanner.Scanner
import com.simprints.fingerprintscanner.ScannerCallback
import com.simprints.fingerprintscanner.ScannerUtils
import com.simprints.fingerprintscanner.ScannerUtils.convertAddressToSerial
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.safe.setup.BluetoothNotEnabledException
import com.simprints.id.exceptions.safe.setup.MultipleScannersPairedException
import com.simprints.id.exceptions.safe.setup.ScannerLowBatteryException
import com.simprints.id.exceptions.safe.setup.ScannerNotPairedException
import com.simprints.id.exceptions.unexpected.BluetoothNotSupportedException
import com.simprints.id.exceptions.unexpected.NullScannerException
import com.simprints.id.exceptions.unexpected.UnknownBluetoothIssueException
import io.reactivex.Completable

open class ScannerManagerImpl(private val preferencesManager: PreferencesManager,
                              private val analyticsManager: AnalyticsManager,
                              private val crashReportManager: CrashReportManager,
                              private val bluetoothAdapter: BluetoothComponentAdapter) : ScannerManager {

    override var scanner: Scanner? = null
    override var macAddress: String? = null
    override var scannerId: String? = null
    override var hardwareVersion: String? = null

    @SuppressLint("CheckResult")
    override fun start(): Completable =
        disconnectVero()
            .andThen(initVero())
            .andThen(connectToVero())
            .andThen(resetVeroUI())
            .andThen(shutdownVero())
            .andThen(wakeUpVero())

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

    override fun initVero(): Completable = Completable.create {
        val pairedScanners = ScannerUtils.getPairedScanners(bluetoothAdapter)
        when {
            pairedScanners.isEmpty() -> it.onError(ScannerNotPairedException())
            pairedScanners.size > 1 -> it.onError(MultipleScannersPairedException())
            else -> {
                val macAddress = pairedScanners[0]
                this.macAddress = macAddress

                scanner = Scanner(macAddress, bluetoothAdapter)
                preferencesManager.lastScannerUsed = convertAddressToSerial(macAddress)

                logMessageForCrashReport("ScannerManager: Scanner initialized")
                it.onComplete()
            }
        }
    }


    override fun connectToVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onError(NullScannerException())
        } else {
            scanner?.connect(WrapperScannerCallback({
                logMessageForCrashReport("ScannerManager: Connected to Vero")
                scannerId = scanner?.scannerId ?: ""
                analyticsManager.logScannerProperties(macAddress ?: "", scannerId?: "")
                result.onComplete()
            }, { scannerError ->
                scannerError?.let {
                    val issue = when (scannerError) {
                        SCANNER_ERROR.BLUETOOTH_DISABLED -> BluetoothNotEnabledException()
                        SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED -> BluetoothNotSupportedException()
                        SCANNER_ERROR.SCANNER_UNBONDED -> ScannerNotPairedException()
                        SCANNER_ERROR.BUSY, SCANNER_ERROR.IO_ERROR -> UnknownBluetoothIssueException()
                        else -> UnknownBluetoothIssueException()
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
                logMessageForCrashReport("ScannerManager: UN20 ready")
                hardwareVersion = scanner?.ucVersion?.toString() ?: "unknown"

                result.onComplete()
            }, { scannerError ->

                scannerError?.let {
                    val issue = when (scannerError) {
                        SCANNER_ERROR.UN20_LOW_VOLTAGE -> {
                            ScannerLowBatteryException()
                        }
                        else -> UnknownBluetoothIssueException()
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
                logMessageForCrashReport("ScannerManager: UN20 off")
                hardwareVersion = scanner?.ucVersion?.toString() ?: "unknown"

                result.onComplete()
            }, {
                result.onError(UnknownBluetoothIssueException())
            }))
        }
    }

    override fun resetVeroUI(): Completable = Completable.create { result ->

        if (scanner == null) {
            result.onError(NullScannerException())
        } else {
            scanner?.resetUI(WrapperScannerCallback({
                logMessageForCrashReport("ScannerManager: UI reset")
                result.onComplete()
            }, { scannerError ->
                scannerError?.let {
                    result.onError(UnknownBluetoothIssueException(it.details()))
                } ?: result.onComplete()
            }))
        }
    }

    override fun getAlertType(it: Throwable): Alert =
        when (it) {
            is BluetoothNotEnabledException -> Alert.BLUETOOTH_NOT_ENABLED
            is BluetoothNotSupportedException -> Alert.BLUETOOTH_NOT_SUPPORTED
            is MultipleScannersPairedException -> Alert.MULTIPLE_PAIRED_SCANNERS
            is ScannerLowBatteryException -> Alert.LOW_BATTERY
            is ScannerNotPairedException -> Alert.NOT_PAIRED
            is UnknownBluetoothIssueException -> Alert.DISCONNECTED
            else -> Alert.UNEXPECTED_ERROR
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

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.SCANNER_SETUP, CrashReportTrigger.SCANNER, message = message)
    }
}

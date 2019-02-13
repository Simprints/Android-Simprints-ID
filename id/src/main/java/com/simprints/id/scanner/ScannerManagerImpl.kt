package com.simprints.id.scanner

import android.annotation.SuppressLint
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashReport.CrashReportTags
import com.simprints.id.data.analytics.crashReport.CrashTrigger
import com.simprints.id.data.analytics.crashReport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.setup.*
import com.simprints.id.exceptions.unsafe.NullScannerError
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.Scanner
import com.simprints.libscanner.ScannerCallback
import com.simprints.libscanner.ScannerUtils
import com.simprints.libscanner.ScannerUtils.convertAddressToSerial
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import io.reactivex.Completable

open class ScannerManagerImpl(val preferencesManager: PreferencesManager,
                              val analyticsManager: AnalyticsManager,
                              val crashReportManager: CrashReportManager,
                              private val bluetoothAdapter: BluetoothComponentAdapter) : ScannerManager {

    override var scanner: Scanner? = null

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
                preferencesManager.macAddress = macAddress

                scanner = Scanner(macAddress, bluetoothAdapter)

                preferencesManager.lastScannerUsed = convertAddressToSerial(macAddress)

                logMessageForCrashReport("ScannerManager: Scanner initialized")
                it.onComplete()
            }
        }
    }

    override fun connectToVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onError(NullScannerError())
        } else {
            scanner?.connect(WrapperScannerCallback({
                logMessageForCrashReport("ScannerManager: Connected to Vero")
                preferencesManager.scannerId = scanner?.scannerId ?: ""
                analyticsManager.logScannerProperties()
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
            result.onError(NullScannerError())
        } else {
            scanner?.un20Wakeup(WrapperScannerCallback({
                logMessageForCrashReport("ScannerManager: UN20 ready")
                preferencesManager.hardwareVersion = scanner?.ucVersion ?: -1

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
            result.onError(NullScannerError())
        } else {
            scanner?.un20Shutdown(WrapperScannerCallback({
                logMessageForCrashReport("ScannerManager: UN20 off")
                preferencesManager.hardwareVersion = scanner?.ucVersion ?: -1

                result.onComplete()
            }, {
                result.onError(UnknownBluetoothIssueException())
            }))
        }
    }

    override fun resetVeroUI(): Completable = Completable.create { result ->

        if (scanner == null) {
            result.onError(NullScannerError())
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

    override fun getAlertType(it: Throwable): ALERT_TYPE =
        when (it) {
            is BluetoothNotEnabledException -> ALERT_TYPE.BLUETOOTH_NOT_ENABLED
            is BluetoothNotSupportedException -> ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED
            is MultipleScannersPairedException -> ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS
            is ScannerLowBatteryException -> ALERT_TYPE.LOW_BATTERY
            is ScannerNotPairedException -> ALERT_TYPE.NOT_PAIRED
            is UnknownBluetoothIssueException -> ALERT_TYPE.DISCONNECTED
            else -> ALERT_TYPE.UNEXPECTED_ERROR
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
        crashReportManager.logInfo(CrashReportTags.SCANNER_SETUP, CrashTrigger.SCANNER, message)
    }
}

package com.simprints.id.controllers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.setup.*
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.Scanner
import com.simprints.libscanner.ScannerCallback
import com.simprints.libscanner.ScannerUtils
import com.simprints.libscanner.ScannerUtils.convertAddressToSerial
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.libscanner.bluetooth.android.AndroidBluetoothAdapter
import io.reactivex.Completable
import timber.log.Timber
import javax.inject.Inject

class ScannerManagerImpl(val preferencesManager: PreferencesManager,
                         val analyticsManager: AnalyticsManager,
                         val bluetoothAdapter: BluetoothComponentAdapter) : ScannerManager {

    override var scanner: Scanner? = null

    @SuppressLint("CheckResult")
    override fun start(): Completable =
        disconnectVero()
        .andThen(initVero())
        .andThen(connectToVero())
        .andThen(resetVeroUI())
        .andThen(wakingUpVero())

    override fun disconnectVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onComplete()
        } else {
            scanner?.disconnect(WrapperScannerCallback({
                result.onComplete()
            }, { _ ->
                result.onComplete()
            }))
        }
    }

    override fun initVero() = Completable.create {
        val pairedScanners = ScannerUtils.getPairedScanners(bluetoothAdapter)
        when {
            pairedScanners.size == 0 -> it.onError(ScannerNotPairedException())
            pairedScanners.size > 1 -> it.onError(MultipleScannersPairedException())
            else -> {
                val macAddress = pairedScanners[0]
                preferencesManager.macAddress = macAddress

                scanner = Scanner(macAddress, bluetoothAdapter)

                preferencesManager.lastScannerUsed = convertAddressToSerial(macAddress)

                Timber.d("Setup: Scanner initialized.")
                it.onComplete()
            }
        }
    }

    override fun connectToVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onError(Throwable("Unexpected error - Scanner null"))
        } else {
            scanner?.connect(WrapperScannerCallback({
                Timber.d("Setup: Connected to Vero.")
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

    override fun wakingUpVero(): Completable = Completable.create { result ->
        if (scanner == null) {
            result.onError(Throwable("Unexpected error - Scanner null"))
        } else {
            scanner?.un20Wakeup(WrapperScannerCallback({
                Timber.d("Setup: UN20 ready.")
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

    // STEP 5
    override fun resetVeroUI(): Completable = Completable.create { result ->

        if (scanner == null) {
            result.onError(Throwable("Unexpected error - Scanner null"))
        } else {
            scanner?.let {
                it.resetUI(WrapperScannerCallback({
                    Timber.d("Setup: UI reset.")
                    result.onComplete()
                }, { scannerError ->
                    scannerError?.let {
                        result.onError(UnknownBluetoothIssueException(it.details()))
                    } ?: result.onComplete()
                }
                ))
            }
        }
    }

    override fun getAlertType(it: Throwable): ALERT_TYPE {
        return when (it) {
            is BluetoothNotEnabledException -> ALERT_TYPE.BLUETOOTH_NOT_ENABLED
            is BluetoothNotSupportedException -> ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED
            is MultipleScannersPairedException -> ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS
            is ScannerLowBatteryException -> ALERT_TYPE.LOW_BATTERY
            is ScannerNotPairedException -> ALERT_TYPE.NOT_PAIRED
            is UnknownBluetoothIssueException -> ALERT_TYPE.DISCONNECTED
            else -> ALERT_TYPE.UNEXPECTED_ERROR
        }
    }

    override fun disconnectScannerIfNeeded() {
        scanner?.disconnect(object : ScannerCallback {
            override fun onSuccess() {}
            override fun onFailure(scanner_error: SCANNER_ERROR) {}
        })
    }

    class WrapperScannerCallback(val success: () -> Unit, val failure: (scannerError: SCANNER_ERROR?) -> Unit) : ScannerCallback {
        override fun onSuccess() {
            success()
        }

        override fun onFailure(error: SCANNER_ERROR?) {
            failure(error)
        }
    }
}

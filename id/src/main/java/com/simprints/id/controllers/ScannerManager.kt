package com.simprints.id.controllers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.events.ScannerConnectionEvent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.safe.setup.*
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.Scanner
import com.simprints.libscanner.ScannerCallback
import com.simprints.libscanner.ScannerUtils
import com.simprints.libscanner.ScannerUtils.convertAddressToSerial
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.libscanner.bluetooth.android.AndroidBluetoothAdapter
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class ScannerManager(val component: AppComponent) {

    var scanner: Scanner? = null

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var bluetoothAdapter: BluetoothComponentAdapter
    @Inject lateinit var sessionEventsManager: SessionEventsManager

    enum class SetupStateDone {
        DISCONNECT_VERO,
        INIT_VERO,
        CONNECTING_TO_VERO,
        RESET_UI,
        WAKING_UP_VERO
    }

    init {
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    fun start(): Observable<SetupStateDone> =
        Observable.create<SetupStateDone> { result ->
            bluetoothAdapter = AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())

            try {
                disconnectVero().doOnSuccess { result.onNext(it) }
                    .flatMap { initVero() }.doOnSuccess { result.onNext(it) }
                    .flatMap { connectToVero() }.doOnSuccess { result.onNext(it) }
                    .flatMap { resetUi() }.doOnSuccess { result.onNext(it) }
                    .flatMap { wakingUpVero() }.doOnSuccess { result.onNext(it) }
                    .blockingGet()

                result.onComplete()
            } catch (e: Exception) {
                result.onError(e)
            }
        }

    private fun disconnectVero(): Single<SetupStateDone> = Single.create { result ->
        scanner?.let {
            it.disconnect(object : ScannerCallback {
                override fun onSuccess() {
                    result.onSuccess(SetupStateDone.DISCONNECT_VERO)
                }

                override fun onFailure(error: SCANNER_ERROR?) {
                    result.onSuccess(SetupStateDone.DISCONNECT_VERO)
                }
            })
        } ?: result.onSuccess(SetupStateDone.DISCONNECT_VERO)
    }

    private fun initVero(): Single<SetupStateDone> = Single.create {
        val pairedScanners = ScannerUtils.getPairedScanners(bluetoothAdapter)
        if (pairedScanners.size == 0) {
            //onAlert(ALERT_TYPE.NOT_PAIRED)
            it.onError(ScannerNotPairedException())
        }

        if (pairedScanners.size > 1) {
            //onAlert(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS)
            it.onError(MultipleScannersPairedException())
        }

        //TODO: Remove when the PreferenceManager session is killed
        val macAddress = pairedScanners[0]
        preferencesManager.macAddress = macAddress

        scanner = Scanner(macAddress, bluetoothAdapter)

        preferencesManager.lastScannerUsed = convertAddressToSerial(macAddress)

        Timber.d("Setup: Scanner initialized.")
        it.onSuccess(SetupStateDone.INIT_VERO)
    }

    private fun connectToVero(): Single<SetupStateDone> = Single.create { result ->

        scanner?.let {
            it.connect(object : ScannerCallback {
                override fun onSuccess() {
                    Timber.d("Setup: Connected to Vero.")
                    preferencesManager.scannerId = it.scannerId ?: ""
                    analyticsManager.logScannerProperties()

                    sessionEventsManager.addEventForScannerConnectivityInBackground(
                        ScannerConnectionEvent.ScannerInfo(
                            preferencesManager.scannerId,
                            preferencesManager.macAddress,
                            preferencesManager.hardwareVersionString))

                    result.onSuccess(SetupStateDone.CONNECTING_TO_VERO)
                }

                override fun onFailure(scanner_error: SCANNER_ERROR) {
                    val issue = when (scanner_error) {
                        SCANNER_ERROR.INVALID_STATE // Already connected, considered as a success
                        -> {
                            Timber.d("Setup: Connected to Vero.")
                            null
                        }
                        SCANNER_ERROR.BLUETOOTH_DISABLED -> BluetoothNotEnabledException()
                        SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED -> BluetoothNotSupportedException()
                        SCANNER_ERROR.SCANNER_UNBONDED -> ScannerNotPairedException()
                        SCANNER_ERROR.BUSY, SCANNER_ERROR.IO_ERROR -> UnknownBluetoothIssueException()
                        else -> UnknownBluetoothIssueException()
                    }
                    issue?.let { issue ->
                        result.onError(issue)
                    } ?: result.onSuccess(SetupStateDone.CONNECTING_TO_VERO)
                    // If invalid state, we ignore the issue. To double check
                    // StopShip
                }
            })
        } ?: result.onError(Throwable("Unexpected error - Scanner null"))
    }

    private fun wakingUpVero(): Single<SetupStateDone> = Single.create { result ->
        scanner?.let {
            it.un20Wakeup(object : ScannerCallback {
                override fun onSuccess() {
                    Timber.d("Setup: UN20 ready.")
                    preferencesManager.hardwareVersion = scanner?.ucVersion ?: -1
                    sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(preferencesManager.hardwareVersionString)

                    result.onSuccess(SetupStateDone.WAKING_UP_VERO)
                }

                override fun onFailure(scanner_error: SCANNER_ERROR) {
                    val issue = when (scanner_error) {
                        SCANNER_ERROR.BUSY, SCANNER_ERROR.INVALID_STATE -> null
                        SCANNER_ERROR.UN20_LOW_VOLTAGE -> {
                            ScannerLowBatteryException()
                        }
                        else -> UnknownBluetoothIssueException()
                    }

                    issue?.let { issue ->
                        result.onError(issue)
                    } ?: result.onSuccess(SetupStateDone.WAKING_UP_VERO)
                }
            })
        } ?: result.onError(Throwable("Unexpected error - Scanner null"))
    }

    // STEP 5
    private fun resetUi(): Single<SetupStateDone> = Single.create { result ->

        scanner?.let {
            it.resetUI(object : ScannerCallback {
                override fun onSuccess() {
                    Timber.d("Setup: UI reset.")
                    result.onSuccess(SetupStateDone.RESET_UI)
                }

                override fun onFailure(scanner_error: SCANNER_ERROR) {
                    val issue = when (scanner_error) {
                        SCANNER_ERROR.BUSY, SCANNER_ERROR.INVALID_STATE -> null
                        else -> UnknownBluetoothIssueException()
                    }

                    issue?.let { issue ->
                        result.onError(issue)
                    } ?: result.onSuccess(SetupStateDone.RESET_UI)
                }
            })
        } ?: result.onError(Throwable("Unexpected error - Scanner null"))
    }

    fun disconnectScannerIfNeeded() {
        scanner?.disconnect(object : ScannerCallback {
            override fun onSuccess() {}
            override fun onFailure(scanner_error: SCANNER_ERROR) {}
        })
    }
}

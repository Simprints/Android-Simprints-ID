package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.NullScannerException
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class ScannerManagerImpl(private val bluetoothAdapter: ComponentBluetoothAdapter,
                         private val scannerFactory: ScannerFactory,
                         private val pairingManager: ScannerPairingManager,
                         private val serialNumberConverter: SerialNumberConverter) : ScannerManager {

    override var scanner: ScannerWrapper? = null
    override var currentScannerId: String? = null
    override var currentMacAddress: String? = null

    override fun <T> onScanner(method: ScannerWrapper.() -> T): T =
        delegateToScannerOrThrow(method)

    override fun scanner(method: ScannerWrapper.() -> Completable): Completable =
        Completable.defer { delegateToScannerOrThrow(method) }

    override fun <T> scanner(method: ScannerWrapper.() -> Single<T>): Single<T> =
        Single.defer { delegateToScannerOrThrow(method) }

    override fun <T> scannerObservable(method: ScannerWrapper.() -> Observable<T>): Observable<T> =
        Observable.defer { delegateToScannerOrThrow(method) }

    private fun <T> delegateToScannerOrThrow(method: ScannerWrapper.() -> T) =
        scanner?.method() ?: throw NullScannerException()

    override fun initScanner(): Completable = Completable.create {
        try {
            val macAddress = pairingManager.getPairedScannerAddressToUse()
            scanner = scannerFactory.create(macAddress)
            currentMacAddress = macAddress
            currentScannerId = serialNumberConverter.convertMacAddressToSerialNumber(macAddress)
            it.onComplete()
        } catch (e: ScannerNotPairedException) {
            it.onError(e)
        } catch (e: MultiplePossibleScannersPairedException) {
            it.onError(e)
        }
    }

    override fun checkBluetoothStatus(): Completable = Completable.create {
        if (!bluetoothIsEnabled()) {
            it.onError(BluetoothNotEnabledException())
        } else {
            it.onComplete()
        }
    }

    private fun bluetoothIsEnabled() = bluetoothAdapter.isEnabled()
}

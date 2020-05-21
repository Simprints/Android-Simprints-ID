package com.simprints.fingerprint.activities.connect.issues.ota

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.tools.livedata.postEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class OtaViewModel(
    private val scannerManager: ScannerManager
) : ViewModel() {

    val progress = MutableLiveData(0f)
    val otaComplete = MutableLiveData<LiveDataEvent>()

    @SuppressLint("CheckResult")
    fun startOta(availableOtas: List<AvailableOta>) {
        scannerManager.scanner { disconnect() }
            .andThen(scannerManager.scanner { connect() })
            .andThen(Observable.concat(availableOtas.map {
                when (it) {
                    AvailableOta.CYPRESS -> scannerManager.scannerObservable { performCypressOta() }
                    AvailableOta.STM -> scannerManager.scannerObservable { performStmOta() }
                    AvailableOta.UN20 -> scannerManager.scannerObservable { performUn20Ota() }
                }
            })).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onNext = { otaStep -> progress.postValue(otaStep.totalProgress) },
                onComplete = {
                    progress.postValue(1f)
                    otaComplete.postEvent()
                },
                onError = ::handleScannerError
            )
    }

    private fun handleScannerError(e: Throwable) {
        // TODO : Show OTA failed cases
        Timber.e(e)
    }
}

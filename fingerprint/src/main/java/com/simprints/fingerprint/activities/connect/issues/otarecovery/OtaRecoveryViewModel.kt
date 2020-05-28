package com.simprints.fingerprint.activities.connect.issues.otarecovery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.tools.livedata.postEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class OtaRecoveryViewModel(private val scannerManager: ScannerManager) : ViewModel() {

    val isConnectionSuccess = MutableLiveData<LiveDataEventWithContent<Boolean>>()

    var reconnectingTask: Disposable? = null

    fun handleTryAgainPressed() {
        reconnectingTask = scannerManager.scanner { disconnect() }
            .andThen(scannerManager.scanner { connect() })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = { isConnectionSuccess.postEvent(true) },
                onError = { isConnectionSuccess.postEvent(false) }
            )
    }

    override fun onCleared() {
        super.onCleared()
        reconnectingTask?.dispose()
    }
}

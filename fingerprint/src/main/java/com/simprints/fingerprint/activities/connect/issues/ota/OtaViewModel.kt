package com.simprints.fingerprint.activities.connect.issues.ota

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.activities.connect.issues.otarecovery.OtaRecoveryFragmentRequest
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.fingerprint.scanner.domain.ota.OtaStep
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
    val otaFailedNeedingUserAction = MutableLiveData<LiveDataEventWithContent<OtaRecoveryFragmentRequest>>()

    private var currentStep: OtaStep? = null
    private var remainingOtas = mutableListOf<AvailableOta>()

    @SuppressLint("CheckResult")
    fun startOta(availableOtas: List<AvailableOta>, currentRetryAttempt: Int) {
        remainingOtas.addAll(remainingOtas)
        scannerManager.scanner { disconnect() }
            .andThen(scannerManager.scanner { connect() })
            .andThen(Observable.concat(availableOtas.map {
                when (it) {
                    AvailableOta.CYPRESS -> scannerManager.scannerObservable { performCypressOta() }
                    AvailableOta.STM -> scannerManager.scannerObservable { performStmOta() }
                    AvailableOta.UN20 -> scannerManager.scannerObservable { performUn20Ota() }
                }.doOnComplete { remainingOtas.remove(it) }
            })).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onNext = { otaStep ->
                    currentStep = otaStep
                    progress.postValue(otaStep.totalProgress)
                },
                onComplete = {
                    progress.postValue(1f)
                    otaComplete.postEvent()
                },
                onError = { handleScannerError(it, currentRetryAttempt) }
            )
    }

    private fun handleScannerError(e: Throwable, currentRetryAttempt: Int) {
        Timber.e(e)
        if (currentRetryAttempt >= MAX_RETRY_ATTEMPTS) {
            handleOtaFailedFinalTime()
        } else {
            when (val strategy = currentStep?.recoveryStrategy) {
                is OtaRecoveryStrategy.UserActionRequired ->
                    otaFailedNeedingUserAction.postEvent(OtaRecoveryFragmentRequest(strategy, remainingOtas, currentRetryAttempt))
                OtaRecoveryStrategy.NoUserActionRequired.Un20OnlyReset ->
                    resetUn20()
                null -> handleOtaFailedFinalTime()
            }
        }

    }

    private fun resetUn20() {
        TODO("Not yet implemented")
    }

    private fun handleOtaFailedFinalTime() {
        TODO()
    }

    companion object {
        const val MAX_RETRY_ATTEMPTS = 1
    }
}

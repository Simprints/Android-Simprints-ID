package com.simprints.fingerprint.connect.screens.ota

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.fingerprint.connect.screens.ota.recovery.OtaRecoveryParams
import com.simprints.fingerprint.connect.usecase.ReportFirmwareUpdateEventUseCase
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy.HARD_RESET
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy.SOFT_RESET
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy.SOFT_RESET_AFTER_DELAY
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaStep
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OtaViewModel @Inject constructor(
    private val scannerManager: ScannerManager,
    private val reportFirmwareUpdate: ReportFirmwareUpdateEventUseCase,
    private val timeHelper: TimeHelper,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val configManager: ConfigManager
) : ViewModel() {

    val progress: LiveData<Float>
        get() = _progress
    private val _progress = MutableLiveData(0f)

    val otaComplete: LiveData<LiveDataEvent>
        get() = _otaComplete
    private val _otaComplete = MutableLiveData<LiveDataEvent>()

    val otaRecovery: LiveData<LiveDataEventWithContent<OtaRecoveryParams>>
        get() = _otaRecovery
    private val _otaRecovery = MutableLiveData<LiveDataEventWithContent<OtaRecoveryParams>>()

    val otaFailed: LiveData<LiveDataEventWithContent<FetchOtaResult>>
        get() = _otaFailed
    private val _otaFailed = MutableLiveData<LiveDataEventWithContent<FetchOtaResult>>()

    private var currentStep: OtaStep? = null
    private val remainingOtas = mutableListOf<AvailableOta>()
    private lateinit var fingerprintSdk: FingerprintConfiguration.BioSdk

    @SuppressLint("CheckResult")
    fun startOta(
        fingerprintSdk: FingerprintConfiguration.BioSdk,
        availableOtas: List<AvailableOta>,
        currentRetryAttempt: Int
    ) {
        remainingOtas.addAll(availableOtas)
        this.fingerprintSdk = fingerprintSdk

        viewModelScope.launch {
            try {
                availableOtas.asFlow()
                    .flatMapConcat {
                        it.toFlowOfSteps().onCompletion { error ->
                            if (error == null) {
                                remainingOtas.remove(it)
                            }
                        }
                    }
                    .onCompletion { error ->
                        if (error == null) {
                            _progress.postValue(1f)
                            _otaComplete.send()
                        }
                    }
                    .collect { otaStep ->
                        Simber.d(otaStep.toString())
                        currentStep = otaStep
                        _progress.postValue(
                            otaStep.totalProgress.mapToTotalProgress(remainingOtas.size, availableOtas.size)
                        )
                    }
            } catch (ex: Throwable) {
                handleScannerError(ex, currentRetryAttempt)
            }
        }
    }

    private suspend fun targetVersions(availableOta: AvailableOta): String {
        val scannerVersion = recentUserActivityManager.getRecentUserActivity().lastScannerVersion
        val availableFirmwareVersions =
            configManager.getProjectConfiguration().fingerprint?.getSdkConfiguration(fingerprintSdk)?.vero2?.firmwareVersions
        return when (availableOta) {
            AvailableOta.CYPRESS -> availableFirmwareVersions?.get(scannerVersion)?.cypress ?: ""
            AvailableOta.STM -> availableFirmwareVersions?.get(scannerVersion)?.stm ?: ""
            AvailableOta.UN20 -> availableFirmwareVersions?.get(scannerVersion)?.un20 ?: ""
        }
    }

    private suspend fun AvailableOta.toFlowOfSteps(): Flow<OtaStep> {
        val otaStartedTime = timeHelper.now()
        val targetVersions = targetVersions(this)

        return when (this) {
            AvailableOta.CYPRESS -> scannerManager.otaOperationsWrapper.performCypressOta(targetVersions)
            AvailableOta.STM -> scannerManager.otaOperationsWrapper.performStmOta(targetVersions)
            AvailableOta.UN20 -> scannerManager.otaOperationsWrapper.performUn20Ota(targetVersions)
        }.onCompletion { error ->
            reportFirmwareUpdate(otaStartedTime, this@toFlowOfSteps, targetVersions, error)
            if (error != null) {
                throw error
            }
        }
    }

    private suspend fun handleScannerError(e: Throwable, currentRetryAttempt: Int) {
        Simber.e(e)
        if (e is BackendMaintenanceException) {
            _otaFailed.send(
                FetchOtaResult(
                    isMaintenanceMode = true,
                    estimatedOutage = e.estimatedOutage
                )
            )
        } else if (currentRetryAttempt >= MAX_RETRY_ATTEMPTS) {
            _otaFailed.send(FetchOtaResult(isMaintenanceMode = false))
        } else {
            when (val strategy = currentStep?.recoveryStrategy) {
                HARD_RESET, SOFT_RESET -> _otaRecovery.send(OtaRecoveryParams(
                    fingerprintSDK = fingerprintSdk,
                    remainingOtas = remainingOtas,
                    currentRetryAttempt = currentRetryAttempt,
                    recoveryStrategy = strategy,
                ))

                SOFT_RESET_AFTER_DELAY -> {
                    delay(OtaRecoveryStrategy.DELAY_TIME_MS)
                    _otaRecovery.send(OtaRecoveryParams(
                        fingerprintSDK = fingerprintSdk,
                        remainingOtas = remainingOtas,
                        currentRetryAttempt = currentRetryAttempt,
                        recoveryStrategy = SOFT_RESET,
                    ))
                }

                null -> _otaFailed.send(FetchOtaResult(isMaintenanceMode = false))
            }
        }
    }

    private fun Float.mapToTotalProgress(nRemainingOtas: Int, nTotalOtas: Int): Float {
        val baseProgress = (nTotalOtas.toFloat() - nRemainingOtas.toFloat()) / nTotalOtas.toFloat()
        return baseProgress + this / nTotalOtas.toFloat()
    }

    companion object {
        const val MAX_RETRY_ATTEMPTS = 1
    }
}

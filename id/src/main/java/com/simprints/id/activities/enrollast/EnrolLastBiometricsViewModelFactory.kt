package com.simprints.id.activities.enrollast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.orchestrator.EnrolmentHelper

class EnrolLastBiometricsViewModelFactory(
    val enrolmentHelper: EnrolmentHelper,
    val timeHelper: TimeHelper,
    val preferencesManager: IdPreferencesManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        EnrolLastBiometricsViewModel(
            enrolmentHelper,
            timeHelper,
            preferencesManager.fingerprintConfidenceThresholds,
            preferencesManager.faceConfidenceThresholds,
            preferencesManager.isEnrolmentPlus
        ) as T
}

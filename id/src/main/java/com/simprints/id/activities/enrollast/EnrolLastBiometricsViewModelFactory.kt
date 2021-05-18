package com.simprints.id.activities.enrollast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.core.tools.time.TimeHelper

class EnrolLastBiometricsViewModelFactory(val enrolmentHelper: EnrolmentHelper,
                                          val timeHelper: TimeHelper, val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        EnrolLastBiometricsViewModel(
            enrolmentHelper,
            timeHelper,
            preferencesManager.fingerprintConfidenceThresholds,
            preferencesManager.faceConfidenceThresholds,
            preferencesManager.isEnrolmentPlus) as T
}

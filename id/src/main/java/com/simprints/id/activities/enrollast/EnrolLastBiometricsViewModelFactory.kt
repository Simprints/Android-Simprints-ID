package com.simprints.id.activities.enrollast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.tools.time.TimeHelper

class EnrolLastBiometricsViewModelFactory(val enrolmentHelper: EnrolmentHelper,
                                          val timeHelper: TimeHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        EnrolLastBiometricsViewModel(enrolmentHelper, timeHelper) as T
}

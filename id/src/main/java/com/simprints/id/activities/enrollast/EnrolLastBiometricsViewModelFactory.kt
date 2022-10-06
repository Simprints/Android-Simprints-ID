package com.simprints.id.activities.enrollast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.activities.faceexitform.FaceExitFormViewModel
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.infra.config.ConfigManager

class EnrolLastBiometricsViewModelFactory(
    val enrolmentHelper: EnrolmentHelper,
    val timeHelper: TimeHelper,
    val configManager: ConfigManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(FaceExitFormViewModel::class.java)) {
            EnrolLastBiometricsViewModel(
                enrolmentHelper,
                timeHelper,
                configManager,
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
}

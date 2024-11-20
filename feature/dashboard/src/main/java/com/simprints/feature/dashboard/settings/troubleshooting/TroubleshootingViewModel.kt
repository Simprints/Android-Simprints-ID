package com.simprints.feature.dashboard.settings.troubleshooting

import androidx.lifecycle.ViewModel
import com.simprints.core.DeviceID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class TroubleshootingViewModel @Inject constructor(
    @DeviceID private val deviceID: String,
) : ViewModel() {

}
